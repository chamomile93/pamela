some sig MySubject {
	PATTERN_ID : one String,
	AUTH_INFO : one String,
	MANAGER : one String,
	ID_PROOF : one String,
	var authInfo : String,
	var idProof : one Int,
	var isAuthenticated : True,
	var manager : one MyAuthenticator
} {
	 PATTERN_ID = "patternIDValue"
	 AUTH_INFO = "auth_infoValue"
	 MANAGER = "manager"
	 ID_PROOF = "id_proof"
	 manager in MyAuthenticator
}

fact {
    MySubject.authInfo in {"user1"+"user2" + "user3" + "user4" + "user5" + "user6" + "user7" + "user8" + "user9" + "user10"}
}

fact authenticationInformationImmutable { // a.k.a. P2 Invariance of authentication information
	{ always all s : MySubject {
		(s.authInfo = s'.authInfo)
		}
	}
}

fact {
    MySubject.idProof in {0+1+2+3+4+5+6+7+8+9}
}

fact idProofUniquenessPerManager { // a.k.a. P1 Uniqueness of authentication information
	{ always all s1, 
	s2 : MySubject {
		s1 != s2 && (s1.manager = s2.manager)
		 =>
		(s1.idProof != s2.idProof)
		}
	}
}


fact authenticatorIsImmutable { // a.k.a P3 Invariance of authenticator
	{ always all s : MySubject {
		(s.manager = s'.manager)
	 	}
	}
}

fact proofOfIdentityIsValid { // a.k.a P4 Validity of the Proof of Identity
	{ always all s : MySubject {
		s.isAuthenticated = True
		and (s.idProof = none) or (s.idProof = expectedProof[s.authInfo])
		}
	}
}

fun expectedProof : String -> one Int {
  "user1" -> 1 + "user2" -> 2 + "user3" -> 3 + "user4" -> 4 + "user5" -> 5 + "user6" -> 6 + "user7" -> 7 + "user8" -> 8 + "user9" -> 9 + "user10" -> 0
}

// P5 ProofOfIdentityIsCorrect
// P6 ProofOfIdentityIsIFFSubject

run {} for 10 but 5 Int