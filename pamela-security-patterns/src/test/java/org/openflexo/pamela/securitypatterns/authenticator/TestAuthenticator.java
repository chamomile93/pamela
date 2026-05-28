package org.openflexo.pamela.securitypatterns.authenticator;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.securitypatterns.authenticator.model.IAuthenticator;
import org.openflexo.pamela.securitypatterns.authenticator.model.ISubject;

import junit.framework.TestCase;

public class TestAuthenticator extends TestCase {

	@Test
	public void testPatternAnalysis() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(ISubject.class);
		assertEquals(1, context.getPatternDefinitions(AuthenticatorPatternDefinition.class).size());
		AuthenticatorPatternDefinition patternDefinition = context.getPatternDefinitions(AuthenticatorPatternDefinition.class).get(0);
		assertEquals(ISubject.PATTERN_ID, patternDefinition.getIdentifier());

		assertEquals(IAuthenticator.class, patternDefinition.authenticatorModelEntity.getImplementedInterface());
		assertEquals(IAuthenticator.class.getMethod("request", String.class), patternDefinition.requestAuthentificationMethod);

		assertEquals(ISubject.class, patternDefinition.subjectModelEntity.getImplementedInterface());
		assertEquals(ISubject.class.getMethod("getAuthInfo"), patternDefinition.authentificationInfoMethod);
		assertEquals(ISubject.class.getMethod("setIdProof", int.class), patternDefinition.proofOfIdentitySetterMethod);
		assertEquals(ISubject.class.getMethod("getManager"), patternDefinition.authenticatorGetterMethod);
		assertEquals(ISubject.class.getMethod("authenticate"), patternDefinition.authenticateMethod);

	}

	@Test
	public void testAuthenticateValid() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(ISubject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		ISubject iSubject = factory.newInstance(ISubject.class, "id1");
		iSubject.setManager(manager);
		manager.addUser(iSubject.getAuthInfo());
		iSubject.authenticate();
		assertEquals(iSubject.getIDProof(), manager.generateFromAuthInfo(iSubject.getAuthInfo()));
		System.out.println("IDProof=" + iSubject.getIDProof());
	}

	@Test
	public void testRequiresAuthentication() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(ISubject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		ISubject iSubject = factory.newInstance(ISubject.class, "id1");
		iSubject.setManager(manager);
		manager.addUser(iSubject.getAuthInfo());
		// We haven't call the authenticate() method, but this method is tagged with "@RequiresAuthentication", thus this call the
		// authenticate() method
		iSubject.thisMethodRequiresToBeAuthenticated();
		assertEquals(iSubject.getIDProof(), manager.generateFromAuthInfo(iSubject.getAuthInfo()));
	}

	@Test
	public void testAuthenticatorInvalidReturn() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(ISubject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		ISubject iSubject = factory.newInstance(ISubject.class, "id");
		iSubject.setManager(manager);
		iSubject.authenticate();
		assertEquals(iSubject.getIDProof(), manager.getDefaultToken());
	}

	@Test
	public void testInstanceDiscovery() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(ISubject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		assertNull(context.getPatternInstances(manager));
		ISubject iSubject = factory.newInstance(ISubject.class, "id");
		assertNull(context.getPatternInstances(manager));
		assertEquals(1, context.getPatternInstances(iSubject).size());
		iSubject.setManager(manager);
		assertEquals(1, context.getPatternInstances(manager).size());
		assertEquals(1, context.getPatternInstances(iSubject).size());
		assertSame(context.getPatternInstances(manager).iterator().next(), context.getPatternInstances(iSubject).iterator().next());
	}

	@Test
	public void testAuthInfoUniqueness() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(ISubject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		ISubject iSubject = factory.newInstance(ISubject.class, manager, "id");
		ISubject iSubject2 = factory.newInstance(ISubject.class, manager, "id2");
		try {
			ISubject iSubject3 = factory.newInstance(ISubject.class, manager, "id");
			fail();
		} catch (ModelExecutionException e) {
			assertTrue(e.getMessage().contains("Subject Invariant Violation: Authentication information are not unique"));
		}
	}

	@Test
	public void testAuthenticatorInvariant() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(ISubject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		ISubject iSubject = factory.newInstance(ISubject.class, manager, "id");
		iSubject.setManager(manager);
		try {
			iSubject.setManager(factory.newInstance(IAuthenticator.class));
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
			if (e.getMessage().compareTo("Subject Invariant Violation: Authenticator has changed since initialization") != 0) {
				fail();
			}
		}
	}

	@Test
	public void testAuthInfoInvariant() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(ISubject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		ISubject iSubject = factory.newInstance(ISubject.class, manager, "id");
		iSubject.setAuthInfo("id");
		try {
			iSubject.setAuthInfo(null);
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
			if (e.getMessage().compareTo("Subject Invariant Violation: Authentication Information has changed since initialization") != 0) {
				fail();
			}
		}
	}

	@Test
	public void testIdProofForgery() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(ISubject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		ISubject iSubject = factory.newInstance(ISubject.class, manager, "id");
		iSubject.setIdProof(-1);
		iSubject.authenticate();
		iSubject.setIdProof(iSubject.getIDProof());
		try {
			iSubject.setIdProof(iSubject.getIDProof() + 1);
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
			if (e.getMessage().compareTo("Subject Invariant Violation: Proof of identity has been forged") != 0) {
				fail();
			}
		}
	}

	@Test
	public void testInvariantValidityWithDynamicPrivilegeRules() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(ISubject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		ISubject iSubject = factory.newInstance(ISubject.class, manager, "id");
		iSubject.authenticate();
		assertEquals(iSubject.getIDProof(), manager.getDefaultToken());
		manager.addUser(iSubject.getAuthInfo());
		iSubject.getAuthInfo();
		iSubject.authenticate();
	}

	@Test
	public void testCoucou() throws Exception {
		PamelaMetaModel context = PamelaMetaModelLibrary.retrieveMetaModel(ISubject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		ISubject iSubject = factory.newInstance(ISubject.class, "id");
		iSubject.setManager(manager);
		// TODO: write a test
		manager.aMethodGuardedWithAPrecondition();
	}

}
