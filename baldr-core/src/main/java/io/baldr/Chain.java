package io.baldr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Chain<T, R> {
    private final Object root;
    private final List<Function> fns;
    private MockInvocation cachedStubInv;

    Chain(Object root, Function<T, R> fn) {
        this.root = root;
        List<Function> list = new ArrayList<>();
        list.add(fn);
        this.fns = Collections.unmodifiableList(list);
    }

    private Chain(Object root, List<Function> parentFns, Function fn) {
        this.root = root;
        List<Function> list = new ArrayList<>(parentFns);
        list.add(fn);
        this.fns = Collections.unmodifiableList(list);
    }

    public <N> Chain<R, N> then(Function<R, N> fn) {
        return new Chain<>(root, fns, fn);
    }

    public Chain<T, R> thenReturn(R value) {
        stubInv().addReturnValue(value);
        return this;
    }

    public Chain<T, R> thenThrow(Throwable t) {
        stubInv().addThrowable(t);
        return this;
    }

    private MockInvocation stubInv() {
        if (cachedStubInv == null) {
            cachedStubInv = runStubChain();
        }
        return cachedStubInv;
    }

    public <N> MockVerification<N> assertCalled(Function<R, N> fn) {
        return runAssertChain(fn);
    }

    private MockInvocation runStubChain() {
        Object cur = root;
        MockInvocation lastInv = null;
        for (int i = 0; i < fns.size(); i++) {
            MockContext.get().enterStubbing();
            try {
                fns.get(i).apply(cur);
                lastInv = MockContext.get().getPrevious().orElseThrow();
            } finally {
                MockContext.get().exitStubbing();
            }
            if (i < fns.size() - 1) {
                MockedObject<?> returnMock = lastInv.getMockShadow().getReturnMock(lastInv);
                lastInv.addReturnValue(returnMock);  // spies need an explicit stub action; mocks fall back to auto-mock otherwise
                cur = returnMock;
            }
        }
        return lastInv;
    }

    private <N> MockVerification<N> runAssertChain(Function<R, N> finalFn) {
        Object cur = root;
        MockInvocation prevInv = null;

        for (Function fn : fns) {
            MockContext.get().enterAssert();
            MockVerificationImpl impl = new MockVerificationImpl<>(cur, prevInv);
            MockContext.get().setCurrentVerificationImpl(impl);
            try {
                cur = fn.apply(cur);
            } finally {
                MockContext.get().exitAssert();
            }
            prevInv = impl.previous;
        }

        MockContext.get().enterAssert();
        MockVerificationImpl finalImpl = new MockVerificationImpl<>(cur, prevInv);
        MockContext.get().setCurrentVerificationImpl(finalImpl);
        try {
            N returnVal = finalFn.apply((R) cur);
            return new MockVerificationImpl<>(returnVal, finalImpl.previous);
        } finally {
            MockContext.get().exitAssert();
        }
    }
}
