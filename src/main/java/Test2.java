import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import akka.util.ByteString;

import java.nio.file.Paths;
import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Test2 {
    public static Sink<String, CompletionStage<IOResult>> lineSink(String filename) {
        return Flow.of(String.class)
                .map(s -> ByteString.fromString(s.toString() + "\n"))
                .toMat(FileIO.toPath(Paths.get(filename)), Keep.right());
    }

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("QuickStart");
        final Materializer materializer = ActorMaterializer.create(system);
        final Source<Integer, NotUsed> source = Source.range(1, 100);

        final Source<BigInteger, NotUsed> factorials = source.scan(BigInteger.ONE, (acc, next) -> acc.multiply(BigInteger.valueOf(next)));
        CompletionStage<IOResult> ioResultCompletionStage = factorials
                .map(num -> ByteString.fromString(num.toString() + "\n"))
                .runWith(FileIO.toPath(Paths.get("factorials.txt")), materializer);
        factorials.map(BigInteger::toString).runWith(lineSink("factorial2.txt"), materializer);
        CompletionStage<Done> doneCompletionStage = source.runForeach(i -> System.out.println(i), materializer);
        doneCompletionStage.thenRun(() -> system.terminate());
    }
}
