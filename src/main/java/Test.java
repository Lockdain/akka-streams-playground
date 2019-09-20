import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.nio.file.Paths;
import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Test {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("QuickStart");
        final Materializer materializer = ActorMaterializer.create(system);
        final Source<Integer, NotUsed> source = Source.range(1, 100);

        final Source<BigInteger, NotUsed> factorials = source.scan(BigInteger.ONE, (acc, next) -> acc.multiply(BigInteger.valueOf(next)));
        CompletionStage<IOResult> ioResultCompletionStage = factorials
                .map(num -> ByteString.fromString(num.toString() + "\n"))
                .runWith(FileIO.toPath(Paths.get("factorials.txt")), materializer);

        CompletionStage<Done> doneCompletionStage = source.runForeach(i -> System.out.println(i), materializer);
        doneCompletionStage.thenRun(() -> system.terminate());
    }

}
