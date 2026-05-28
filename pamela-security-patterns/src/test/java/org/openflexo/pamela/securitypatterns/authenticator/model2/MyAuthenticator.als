module this/model2/MyAuthenticator

one sig MyAuthenticator {
	ID : String,
	var users : set String,
} {
	{ ID = "authentificatorId" }
}

fun digest[id:String] : String { id }

/*
// Request authentication method
pred request(id : String) {
    (id->digest[id]) in
	("subjectId" -> digest["subjectId"])
}
*/
