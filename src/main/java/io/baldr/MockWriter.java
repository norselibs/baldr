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

import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public class MockWriter extends AutoMapperClassWriter {
	private Clazz superClazz;
	Clazz wrapperGenerated;
	Clazz wrappeeClass;

	public MockWriter(String className, Class<?> tClass) throws NoSuchMethodException {
		super(tClass);
		postFix = "$Baldr$Mock";
		className = className+postFix;
		this.wrappeeClass = Clazz.of(tClass);
		wrapperGenerated = Clazz.of(className);
		this.superClazz = this.wrapperClazz.isInterface() ? Clazz.of(Object.class) : this.wrapperClazz;
		visit(Opcodes.V1_8
				, Access.Public.getOpCode()
				, className
				, null
				, superClazz.getInternalName()
				, Stream.concat(
						Stream.of(Clazz.ofClazzes(MockedObject.class, wrappeeClass).getInternalName())
						, this.wrapperClazz.isInterface() ? Stream.of(this.wrapperClazz.getInternalName()) : Stream.empty()).toArray(String[]::new)		);

		field(Access.Private, "_invocations", Clazz.raw(MockInvocations.class), null);

		MethodWriter mw = method(Access.Public, new MethodSignature(wrapperGenerated, "<init>", Clazz.getVoid()));
		mw.load(0);
		mw.invoke(new MethodSignature(wrappeeClass.clazz.getConstructor()));
		mw.load(0);
		mw.invoke(MockInvocations.class.getMethod("get"));
		mw.putfield(wrapperGenerated, "_invocations", Clazz.raw(MockInvocations.class));
		mw.returnNothing();
		mw.end();
		build();
	}


	protected void build() {
		buildMockMethods();
		buildMethods();
	}

	private void buildMockMethods() {
		try {
			ClazzMethod cm = new ClazzMethod(Clazz.of(MockedObject.class), MockedObject.class.getMethod("$getInvocations"));
			if (!wrapperClazz.declaresMethod(cm)) {
				MethodWriter mw = method(Access.Public, cm.getSignature());
				mw.load(0);
				mw.getField(wrapperGenerated, "_invocations", Clazz.raw(MockInvocations.class));
				mw.returnObject();
				mw.end();
			} else {
				throw new Exception("A mock of a mock is not expected");
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void buildMethods() {
		wrappeeClass.methods().forEach(cm -> {
			try {
				if (!Clazz.of(Object.class).declaresMethod(cm)) {
					MethodWriter mw = method(cm.getAccess(), cm.getSignature());
					mw.load(0);
					mw.getField(wrapperGenerated, "_invocations", Clazz.raw(MockInvocations.class));
					mw.load(0);
					mw.cast(wrappeeClass);
					mw.push(cm.getName());
					mw.invoke(MockInvocations.class.getMethod("buildInvocation", Object.class, String.class));
					int i = 0;
					for (ClazzMethodParameter p : cm.parameters()) {
						mw.dup();
						mw.push(p.getClazz());
						mw.push(cm.getName());
						mw.load(++i, p.getClazz());
						mw.invoke(MockInvocation.class.getMethod("addParameter", Class.class, String.class, Object.class));
					}


					if (cm.getReturnType() != null) {
						mw.invoke(MockInvocation.class.getMethod("end"));
						if(cm.getReturnType().isPrimitive()) {
							mw.unbox(cm.getReturnType());
						} else {
							mw.cast(cm.getReturnType());
						}

						mw.returnOf(cm.getReturnType());
					} else {
						mw.invoke(MockInvocation.class.getMethod("end"));

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
