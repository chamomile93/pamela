package org.openflexo.pamela.securitypatterns.authenticator;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntityLibrary;
import org.openflexo.pamela.securitypatterns.authenticator.model.IAuthenticator;
import org.openflexo.pamela.securitypatterns.authenticator.model.ISubject;

import junit.framework.TestCase;

public class TestAuthenticator extends TestCase {

	private PamelaModelFactory factory;
	private PamelaMetaModel pamelaMetaModel;
	private IAuthenticator manager;
	private ISubject subject;

	protected void clearModelEntityLibrary() {
		ModelEntityLibrary.clear();
		PamelaMetaModelLibrary.clearCache();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		clearModelEntityLibrary();
		pamelaMetaModel = PamelaMetaModelLibrary.retrieveMetaModel(ISubject.class);
		factory = new PamelaModelFactory(pamelaMetaModel);	
		manager = factory.newInstance(IAuthenticator.class);
		subject = factory.newInstance(ISubject.class, "id");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		clearModelEntityLibrary();
	}

	@Test
	public void testPatternAnalysis() throws Exception {
		assertEquals(1, pamelaMetaModel.getPatternDefinitions(AuthenticatorPatternDefinition.class).size());
		AuthenticatorPatternDefinition patternDefinition = pamelaMetaModel.getPatternDefinitions(AuthenticatorPatternDefinition.class).get(0);
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

		subject.setManager(manager);
		manager.addUser(subject);
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
		System.out.println("IDProof=" + subject.getIDProof());
	}

	@Test
	public void testRequiresAuthentication() throws Exception {
		subject.setManager(manager);
		manager.addUser(subject);
		// We haven't call the authenticate() method, but this method is tagged with "@RequiresAuthentication", thus this call the
		// authenticate() method
		subject.thisMethodRequiresToBeAuthenticated();
		assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
	}

	@Test
	public void testAuthenticatorInvalidReturn() throws Exception {
		subject.setManager(manager);
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.getDefaultToken());
	}

	@Test
	public void testInstanceDiscovery() throws Exception {
		assertNull(pamelaMetaModel.getPatternInstances(manager));
		assertNull(pamelaMetaModel.getPatternInstances(manager));
		assertEquals(1, pamelaMetaModel.getPatternInstances(subject).size());
		subject.setManager(manager);
		assertEquals(1, pamelaMetaModel.getPatternInstances(manager).size());
		assertEquals(1, pamelaMetaModel.getPatternInstances(subject).size());
		assertSame(pamelaMetaModel.getPatternInstances(manager).iterator().next(), pamelaMetaModel.getPatternInstances(subject).iterator().next());
	}

	@Test
	public void testAuthInfoUniqueness() throws Exception {
		ISubject subject1 = factory.newInstance(ISubject.class, manager, "id1");
		ISubject subject2 = factory.newInstance(ISubject.class, manager, "id2");
		try {
			ISubject subject3 = factory.newInstance(ISubject.class, manager, "id");
			fail();
		} catch (ModelExecutionException e) {
			assertTrue(e.getMessage().contains("Subject Invariant Violation: Authentication information are not unique"));
		}
	}

	@Test
	public void testAuthenticatorInvariant() throws Exception {
		subject.setManager(manager);
		subject.authenticate();
		try {
			IAuthenticator anotherManager = factory.newInstance(IAuthenticator.class);
			anotherManager.setName("Bob");
			subject.setManager(anotherManager);
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
		subject.setManager(manager);
		try {
			subject.setAuthInfo("id1");
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
		subject.setManager(manager);
		subject.setIdProof(-1);
		manager.addUser(subject);
		subject.authenticate();
		subject.setIdProof(subject.getIDProof());
		try {
			subject.setIdProof(subject.getIDProof() + 1);
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
		subject.setManager(manager);
		
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.getDefaultToken());
		manager.addUser(subject);
		subject.getAuthInfo();
		subject.authenticate();
	}

	@Test
	public void testCoucou() throws Exception {
		subject.setManager(manager);
		// TODO: write a test
		manager.aMethodGuardedWithAPrecondition();
	}

}
