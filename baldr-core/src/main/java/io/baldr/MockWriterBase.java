/* Copyright 2021 PSQR
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.baldr;

import io.ran.*;
import org.objectweb.asm.Opcodes;

import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public abstract class MockWriterBase extends AutoMapperClassWriter {
	Clazz superClazz;
	String className;
	Clazz wrapperGenerated;
	Clazz wrappeeClass;

	public MockWriterBase(String className, Class<?> tClass) throws NoSuchMethodException {
		super(tClass);

		field(Access.Private, "_invocations", Clazz.of(MockShadow.class), null);
		field(Access.Private, "_name", Clazz.of(String.class), null);
	}


	protected void build() throws NoSuchMethodException {
		buildConstructor();
		buildMockMethods();
		buildMethods();
	}

	protected abstract void buildConstructor();


	protected void buildMockMethods() {
		try {
			ClazzMethod cm = new ClazzMethod(Clazz.of(MockedObject.class), MockedObject.class.getMethod("$getShadow"));
			if (!wrapperClazz.declaresMethod(cm)) {
				MethodWriter mw = method(Access.Public, cm.getSignature());
				mw.load(0);
				mw.getField(wrapperGenerated, "_invocations", Clazz.of(MockShadow.class));
				mw.returnObject();
				mw.end();
			} else {
				throw new Exception("A mock of a mock is not expected");
			}

			cm = new ClazzMethod(Clazz.of(MockedObject.class), MockedObject.class.getMethod("$setName", String.class));
			if (!wrapperClazz.declaresMethod(cm)) {
				MethodWriter mw = method(Access.Public, cm.getSignature());
				mw.load(0);
				mw.load(1);
				mw.putfield(wrapperGenerated, "_name", Clazz.of(String.class));
				mw.returnNothing();
				mw.end();
			} else {
				throw new Exception("A mock of a mock is not expected");
			}

			cm = new ClazzMethod(Clazz.of(MockedObject.class), MockedObject.class.getMethod("$getName"));
			if (!wrapperClazz.declaresMethod(cm)) {
				MethodWriter mw = method(Access.Public, cm.getSignature());
				mw.load(0);
				mw.getField(wrapperGenerated, "_name", Clazz.of(String.class));
				mw.returnObject();
				mw.end();
			} else {
				throw new Exception("A mock of a mock is not expected");
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void buildMethods() {
		wrappeeClass.methods().forEach(cm -> {
			try {
				if (!Clazz.of(Object.class).declaresMethod(cm)) {
					MethodWriter mw = method(cm.getAccess(), cm.getSignature());
					mw.load(0);
					mw.getField(wrapperGenerated, "_invocations", Clazz.of(MockShadow.class));
					mw.load(0);
					mw.cast(wrappeeClass);
					mw.push(cm.getName());
					mw.invoke(MockShadow.class.getMethod("buildInvocation", Object.class, String.class));
					int i = 0;
					for (ClazzMethodParameter p : cm.parameters()) {
						mw.dup();
						if (p.getClazz().isPrimitive()) {
							mw.push(p.getClazz().getPrimitive().getDescriptor());
							mw.push(cm.getName());
							mw.load(++i, p.getClazz());
							mw.box(p.getClazz());
							mw.invoke(MockInvocation.class.getMethod("addParameter", String.class, String.class, Object.class));
						} else {
							mw.push(p.getClazz());
							mw.push(cm.getName());
							mw.load(++i, p.getClazz());
							mw.invoke(MockInvocation.class.getMethod("addParameter", Class.class, String.class, Object.class));
						}
					}


					if (!cm.getReturnType().isVoid()) {
						mw.invoke(MockInvocation.class.getMethod("end"));
						mw.invoke(InvocationResult.class.getMethod("get"));
						if(cm.getReturnType().isPrimitive()) {
							mw.unbox(cm.getReturnType());
						} else {
							mw.cast(cm.getReturnType());
						}

						mw.returnOf(cm.getReturnType());
					} else {
						mw.invoke(MockInvocation.class.getMethod("end"));
						mw.pop();
						mw.returnNothing();
					}

					mw.end();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

	}

}
