package org.openflexo.pamela.test.runtime;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.test.library.Book;

public class RuntimeMethodProxyTest {

	@Test
	public void runtimeMethodCanBeRegisteredAndInvoked() throws Exception {
		PamelaModelFactory factory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(Book.class));
		Book book = factory.newInstance(Book.class);
		AccessibleProxyObject proxy = (AccessibleProxyObject) book;
		
		assertFalse(proxy.hasRuntimeMethod("greet"));
		
		proxy.registerRuntimeMethod("greet", (receiver, args) -> "Hello " + args[0]);

		assertTrue(proxy.hasRuntimeMethod("greet"));
		assertEquals("Hello world", proxy.invokeRuntimeMethod("greet", "world"));
	}
}
