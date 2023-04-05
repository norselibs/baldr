/* Copyright 2021 PSQR
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.baldr.hamcrest;

import io.baldr.MockInvocation;
import io.baldr.MockShadow;
import io.baldr.MockedObject;
import io.ran.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.objectweb.asm.Opcodes;

import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public class MatcherWriter extends AutoMapperClassWriter {
	private Clazz superClazz;
	Clazz wrapperGenerated;
	Clazz wrappeeClass;

	public MatcherWriter(String className, Class<?> tClass) throws NoSuchMethodException {
		super(tClass);
		postFix = "$Baldr$Matcher";
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
						Stream.concat(
								Stream.of(Clazz.ofClazzes(Matcher.class, wrappeeClass).getInternalName()),
								Stream.of(Clazz.of(BaldrMatcher.class).getInternalName()))
						, this.wrapperClazz.isInterface() ? Stream.of(this.wrapperClazz.getInternalName()) : Stream.empty()).toArray(String[]::new)		);

		field(Access.Private, "matcher", Clazz.raw(Matcher.class), null);

		MethodWriter mw = method(Access.Public, new MethodSignature(wrapperGenerated, "<init>", Clazz.getVoid()));
		mw.load(0);
		mw.invoke(new MethodSignature(wrappeeClass.clazz.getConstructor()));
		mw.returnNothing();
		mw.end();
		build();
	}


	protected void build() {
		buildMatcherMethods();
		buildToString();
	}

	private void buildToString() {
		try {
			ClazzMethod cm = new ClazzMethod(wrapperGenerated, Object.class.getMethod("toString"));
			MethodWriter mw = method(Access.Public, cm.getSignature());
			mw.load(0);
			mw.getField(wrapperGenerated, "matcher", Clazz.raw(Matcher.class));
			mw.cast(Clazz.of(Object.class));
			mw.invoke(Object.class.getMethod("toString"));
			mw.returnOf(Clazz.of(String.class));
			mw.end();

		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}

	}

	private void buildMatcherMethods() {
		try {
			ClazzMethod cm = new ClazzMethod(Clazz.of(Matcher.class), Matcher.class.getMethod("matches", Object.class));
			if (!wrapperClazz.declaresMethod(cm)) {
				MethodWriter mw = method(Access.Public, cm.getSignature());
				mw.load(0);
				mw.getField(wrapperGenerated, "matcher", Clazz.raw(Matcher.class));
				mw.load(1);
				mw.invoke(Matcher.class.getMethod("matches", Object.class));
				mw.returnPrimitive(Clazz.of(boolean.class));
				mw.end();
			} else {
				throw new Exception("A matcher of a matcher object is not expected");
			}

			cm = new ClazzMethod(Clazz.of(Matcher.class), Matcher.class.getMethod("describeMismatch", Object.class, Description.class));
			if (!wrapperClazz.declaresMethod(cm)) {
				MethodWriter mw = method(Access.Public, cm.getSignature());
				mw.load(0);
				mw.getField(wrapperGenerated, "matcher", Clazz.raw(Matcher.class));
				mw.load(1);
				mw.load(2);
				mw.invoke(Matcher.class.getMethod("describeMismatch", Object.class, Description.class));
				mw.returnNothing();
				mw.end();
			} else {
				throw new Exception("A matcher of a matcher object is not expected");
			}

			cm = new ClazzMethod(Clazz.of(Matcher.class), Matcher.class.getMethod("_dont_implement_Matcher___instead_extend_BaseMatcher_"));
			if (!wrapperClazz.declaresMethod(cm)) {
				MethodWriter mw = method(Access.Public, cm.getSignature());
				mw.returnNothing();
				mw.end();
			} else {
				throw new Exception("A matcher of a matcher object is not expected");
			}

			cm = new ClazzMethod(Clazz.of(BaldrMatcher.class), BaldrMatcher.class.getMethod("setMatcher", Matcher.class));
			if (!wrapperClazz.declaresMethod(cm)) {
				MethodWriter mw = method(Access.Public, cm.getSignature());
				mw.load(0);
				mw.load(1);
				mw.putfield(wrapperGenerated, "matcher", Clazz.of(Matcher.class));
				mw.returnNothing();
				mw.end();
			} else {
				throw new Exception("A matcher of a matcher object is not expected");
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


}
