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
public class MockWriter extends MockWriterBase {

	public MockWriter(String className, Class<?> tClass) throws NoSuchMethodException {
		super(className, tClass);
		postFix = "$Baldr$Mock";
		this.className = className+postFix;
		this.wrappeeClass = Clazz.of(tClass);
		wrapperGenerated = Clazz.of(this.className);
		this.superClazz = this.wrapperClazz.isInterface() ? Clazz.of(Object.class) : this.wrapperClazz;
		build();
	}

	protected void buildConstructor()  {
		try {
			visit(Opcodes.V1_8
					, Access.Public.getOpCode()
					, className
					, null
					, superClazz.getInternalName()
					, Stream.concat(
							Stream.of(Clazz.ofClazzes(MockedObject.class, wrappeeClass).getInternalName())
							, this.wrapperClazz.isInterface() ? Stream.of(this.wrapperClazz.getInternalName()) : Stream.empty()).toArray(String[]::new));

			MethodWriter mw = method(Access.Public, new MethodSignature(wrapperGenerated, "<init>", Clazz.getVoid()));

			List<Constructor> cts = Arrays.asList(wrappeeClass.clazz.getDeclaredConstructors());
			Constructor constructor = cts.stream().sorted(Comparator.comparing(Constructor::getParameterCount)).findFirst().orElse(null);
			if(constructor != null) {
				mw.load(0);
				Arrays.stream(constructor.getParameterTypes()).forEach(p -> {
					if (p.isPrimitive()) {
						mw.push(Clazz.of(p).getDefaultValue());
					} else {
						mw.nullConst();
					}
				});

				mw.invoke(new MethodSignature(constructor));
			} else {
				mw.load(0);
				mw.invoke(new MethodSignature(Object.class.getConstructor()));
			}
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



}
