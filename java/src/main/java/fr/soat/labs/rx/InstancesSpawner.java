package fr.soat.labs.rx;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import com.github.ryenus.rop.OptionParser;
import fr.soat.labs.rx.handler.WebSocketClientOperation;
import org.webbitserver.netty.WebSocketClient;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import sun.plugin2.util.SystemUtil;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 22/03/14
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
@OptionParser.Command(name = "spawner", descriptions = "Will spawn N instances of web server. Will recreate it if servers are killed")
public class InstancesSpawner {

    @OptionParser.Option(opt = {"-i", "--instances"}, description = "number of instances to spawn")
    private int numberOfInstances = 5;
    @OptionParser.Option(opt = {"-p", "--minPort"}, description = "First port to use")
    private int minPort = 4567;
    @OptionParser.Option(opt = {"-m", "--master"}, description = "Master url")
    private String master = "ws://localhost:4444/killed";
    @OptionParser.Option(opt = {"--port"}, description = "Instance spawner port")
    private int port = 4040;

    public static void main(String[] args) {
        OptionParser parser = new OptionParser(InstancesSpawner.class);
        parser.parse(args);
    }

    public void run(OptionParser parser, String[] params) throws Exception { // either or both args can be omitted

        String java = SystemUtil.getJavaHome() + "/bin/java";

        URL[] classpath = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
        String classpathCmd = Observable.from(classpath)
                .reduce("", (str, url) -> {
                    try {
                        return new File(url.toURI()).getAbsolutePath() + File.pathSeparatorChar + str;
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toBlockingObservable()
                .first();

        Subject<String, String> newPortToSpawn = PublishSubject.create();

        Observable<Integer> startingPort = Observable.merge(
                Observable.range(minPort, numberOfInstances),
                newPortToSpawn.map(Integer::parseInt)
        );

        Observable<Process> process = startingPort.map((p) -> {
            try {
                System.out.println("STARTING " + p);
                ProcessBuilder builder = new ProcessBuilder(java,
                        "-classpath", "\"" + classpathCmd + "\"",
                        Node.class.getName(),
                        "-p", p.toString());
                // System.out.println(Observable.from(builder.command()).reduce("", (seed, value) -> seed + " "+value).toBlockingObservable().first());
                builder.redirectErrorStream(true);
                return builder.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        process.subscribe();

//        Observable<Integer> startedNode = Observable.zip(startingPort, process, (port, p) -> port);
//
//        startedNode.subscribe((port) -> System.out.println("Starting new process on port " + port),
//                Throwable::printStackTrace);

        WebSocketClient client = new WebSocketClient(new URI(master), new WebSocketClientOperation(newPortToSpawn));
        Observable.from(client.start()).subscribe((c) -> System.out.println("starting listening on " + c.getUri()));

    }


}
