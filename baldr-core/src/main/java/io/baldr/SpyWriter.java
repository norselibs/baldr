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

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public class SpyWriter extends MockWriterBase {

	public SpyWriter(String className, Class<?> tClass) throws NoSuchMethodException {
		super(className, tClass);
		postFix = "$Baldr$Spy";
		this.className = className+postFix;
		this.wrappeeClass = Clazz.of(tClass);
		wrapperGenerated = Clazz.of(this.className);
		this.superClazz = this.wrapperClazz.isInterface() ? Clazz.of(Object.class) : this.wrapperClazz;
		field(Access.Private, "_instance", Clazz.of(tClass), null);
		build();
	}

	protected void buildConstructor() {
		visit(Opcodes.V1_8
				, Access.Public.getOpCode()
				, className
				, null
				, superClazz.getInternalName()
				, Stream.concat(
						Stream.of(Clazz.ofClazzes(SpiedObject.class, wrappeeClass).getInternalName())
						, this.wrapperClazz.isInterface() ? Stream.of(this.wrapperClazz.getInternalName()) : Stream.empty()).toArray(String[]::new)		);


	try {
		MethodWriter mw = method(Access.Public, new MethodSignature(wrapperGenerated, "<init>", Clazz.getVoid()));
		mw.load(0);
		List<Constructor> cts = Arrays.asList(wrappeeClass.clazz.getDeclaredConstructors());
		Constructor constructor = cts.stream().sorted(Comparator.comparing(Constructor::getParameterCount)).findFirst().orElseThrow(() -> new RuntimeException("Could not find a constructor"));
		Arrays.stream(constructor.getParameterTypes()).forEach(p -> {
			if(p.isPrimitive()) {
				mw.push(Clazz.of(p).getDefaultValue());
			} else {
				mw.nullConst();
			}
		});

		mw.invoke(new MethodSignature(constructor));
		mw.load(0);
		mw.load(0);
		mw.invoke(MockShadow.class.getMethod("get", Object.class));
		mw.putfield(wrapperGenerated, "_invocations", Clazz.raw(MockShadow.class));
		mw.returnNothing();
		mw.end();
	} catch (Exception e) {
		throw new RuntimeException(e);
	}

	}

	protected void buildMockMethods() {
		super.buildMockMethods();
		try {
			ClazzMethod cm = new ClazzMethod(Clazz.of(SpiedObject.class), SpiedObject.class.getMethod("$setInstance", Object.class));
			if (!wrapperClazz.declaresMethod(cm)) {
				MethodWriter mw = method(Access.Public, cm.getSignature());
				mw.load(0);
				mw.load(1);
				mw.cast(wrappeeClass);
				mw.putfield(wrapperGenerated, "_instance", wrappeeClass);
				mw.returnNothing();
				mw.end();
			} else {
				throw new Exception("A spy of a spy is not expected");
			}

			cm = new ClazzMethod(Clazz.of(SpiedObject.class), SpiedObject.class.getMethod("$getInstance"));
			if (!wrapperClazz.declaresMethod(cm)) {
				MethodWriter mw = method(Access.Public, cm.getSignature());
				mw.load(0);
				mw.getField(wrapperGenerated, "_instance", wrappeeClass);
				mw.returnOf(wrappeeClass);
				mw.end();
			} else {
				throw new Exception("A spy of a spy is not expected");
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
						if (p.getClazz().isPrimitive() || p.getClazz().isBoxedPrimitive()) {
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
						mw.dup();
						mw.invoke(InvocationResult.class.getMethod("isPresent"));
						mw.ifThen(ifTrue -> {
							ifTrue.invoke(InvocationResult.class.getMethod("get"));
							if(cm.getReturnType().isPrimitive() || cm.getReturnType().isBoxedPrimitive()) {
								ifTrue.unbox(cm.getReturnType());
							} else {
								ifTrue.cast(cm.getReturnType());
								if (
									!cm.getReturnType().isPrimitive()
									&& !cm.getReturnType().isBoxedPrimitive()
									&& !cm.getReturnType().getSimpleName().equals("String")
								) {
									mw.dup();
									ifTrue.invoke(Baldr.class.getMethod("spy", Object.class));
									ifTrue.cast(cm.getReturnType());
								}
							}
							ifTrue.returnOf(cm.getReturnType());
						});

						mw.invoke(MockContext.class.getMethod("pauseAssert"));
						mw.load(0);
						mw.getField(wrapperGenerated, "_instance", wrappeeClass);
						int parameterNumber=0;
						for (ClazzMethodParameter p : cm.parameters()) {
							mw.load(++parameterNumber, p.getClazz());
						}
						mw.invoke(cm.getMethod());

						if (
								!cm.getReturnType().isPrimitive()
										&& !cm.getReturnType().isBoxedPrimitive()
										&& !cm.getReturnType().getSimpleName().equals("String")
						) {
							mw.dup();
							mw.invoke(Baldr.class.getMethod("spy", Object.class));
							mw.cast(cm.getReturnType());
						}
						mw.invoke(MockContext.class.getMethod("resumeAssert"));
						mw.returnOf(cm.getReturnType());

					} else {
						mw.invoke(MockInvocation.class.getMethod("end"));
						mw.invoke(MockContext.class.getMethod("pauseAssert"));
						mw.load(0);
						mw.getField(wrapperGenerated, "_instance", wrappeeClass);
						int parameterNumber=0;
						for (ClazzMethodParameter p : cm.parameters()) {
							mw.load(++parameterNumber, p.getClazz());
						}
						mw.invoke(cm.getMethod());
						mw.invoke(MockContext.class.getMethod("resumeAssert"));
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
