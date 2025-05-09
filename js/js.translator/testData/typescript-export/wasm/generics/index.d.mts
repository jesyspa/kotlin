type Nullable<T> = T | null | undefined
declare function KtSingleton<T>(): T & (abstract new() => any);
export declare function simple<T>(x: T): T;
export declare function second<A, B>(a: A, b: B): B;
export declare function simpleWithConstraint<T extends number>(x: T): T;
export declare function complexConstraint<A extends not.exported.Foo<bigint> & not.exported.Bar, B extends typeof not.exported.Baz>(x: A): B;
declare namespace not.exported {
    interface Foo<T extends NonNullable<unknown>> {
        readonly foo: T;
    }
}
declare namespace not.exported {
    interface Bar {
        readonly bar: string;
    }
}
declare namespace not.exported {
    abstract class Baz extends KtSingleton<Baz.$metadata$.constructor>() {
        private constructor();
    }
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace Baz.$metadata$ {
        abstract class constructor {
            get baz(): boolean;
            private constructor();
        }
    }
}
