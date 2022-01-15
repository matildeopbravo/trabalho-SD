package sd.server;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

final class Either<L,R>
{
    public static <L,R> Either<L,R> left(L value) {
        return new Either<>(Optional.of(value), Optional.empty());
    }
    public static <L,R> Either<L,R> right(R value) {
        return new Either<>(Optional.empty(), Optional.of(value));
    }
    private final Optional<L> left;
    private final Optional<R> right;
    private Either(Optional<L> l, Optional<R> r) {
        left = l;
        right = r;
    }

    public boolean isRight() {
        return right.isPresent();
    }

    public  boolean isLeft() {
        return left.isPresent();
    }
    public void apply(Consumer<? super L> lFunc, Consumer<? super R> rFunc)
    {
        left.ifPresent(lFunc);
        right.ifPresent(rFunc);
    }

    public R getRight() {
        return right.get();
    }

    public L getLeft() {
        return left.get();
    }
}