function login() {
	 document.loginForm.userid.value = "FAAdmin";
	 document.loginForm.password.value = "Oracle123";
	 document.loginForm.action ="/oam/server/auth_cred_submit";
	 document.loginForm.submit();
};

return login();