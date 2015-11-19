// TODO: move this into a separate javascript file, and then into an API that
//       we can call so the server will tell us about installed plugins
var DIRECTOR_PAGES = [
    {"href": "navbar.html", "target": "#navbar"},
    {"href": "dashboard.html", "target": "#main", "target_tab": "dashboard"}, // TODO:  target_tab should be calculated automatically or generated and synchronized with the links ... the links should be generated from this list too...
    {"href": "settings.html", "target": "#main", "target_tab": "settings"},
    {"href": "vm_images_page.html", "target": "#main", "target_tab": "create_policy_wizard"},
    {"href": "help.html", "target": "#main", "target_tab": "help"},
    {"href": "logout.html", "target": "#main", "target_tab": "logout"},
    {"href": "settings_upload_image_to_director.html", "target": "#main", "target_tab": "settings_upload_image_to_director"},
    {"href": "setting_create_ssh.html", "target": "#main", "target_tab": "setting_create_ssh"},
    {"href": "license.html", "target": "#main", "target_tab": "license"},
    {"href": "/v1/resources/profile.html", "target": "#main", "target_tab": "my_profile"},
    {"href": "/v1/resources/users.html", "target": "#main", "target_tab": "users"},
    {"href": "/v1/resources/saml_certificates.html", "target": "#main", "target_tab": "saml_certificates"},
    {"href": "/v1/resources/tpm_identity_certificates.html", "target": "#main", "target_tab": "tpm_identity_certificates"},
    {"href": "bm_images_page.html", "target": "#main", "target_tab": "create_policy_wizard_for_hosts"}
];

function UserProfile(data) {
    this.username = ko.observable(data.username);
    this.authorizationToken = ko.observable(data.authorizationToken);
    this.authenticated = ko.observable(false);
    this.error = ko.observable(data.error);
}

function LoginRequest(data) {
    this.username = ko.observable(data.username);
    this.password = ko.observable(data.password);
    this.remember_me = ko.observable(data.remember_me);
    this.error = ko.observable(data.error);
}

function LoginViewModel() {
    var self = this;
    self.loginRequest = new LoginRequest({});
    self.userProfile = new UserProfile({});
    self.options = {
        "postLoginActivatePage": "vm_images_page.html",  // dashboard should be the first view we show after login; dashboard can then load the "run once" or "are there notifications" code.
        "postLogoutRedirect": "index.html" //  where to send user after logout is done
    };

    // operations
    self.login = function(loginFormElement) {
    	
    
        console.log("Endpoint: %s", endpoint);
        //            console.log("Search keys 1: %O", ko.toJSON(searchCriteriaItem)); //   results in error: InvalidStateError: Failed to read the 'selectionDirection' property from 'HTMLInputElement': The input element's type ('hidden') does not support selection
        console.log("Login request to %s: %O", endpoint+"/login", ko.toJSON(self.loginRequest)); // attempting to serialize loginRequestItem produces errors, probably because it represents the entire form
//                    console.log("Login request form: %O", loginFormElement); // we could use jquery validation with the form element

     ///   console.log("Login results: %O", data);
        /*
         * Example:
         * {authorization_token: "AclEx5EuuEIxgDYLto/7vimX3LvUvXT66IH4mtnFT0OLTD3bP+4mfylcH6SbfUnhwhTvCk7jt6Ez8H13tBMt0MGZE+bxOppqXyPa9RWH+5irIKj1GuAVE+Y/j4r25zoFXvhr9YqkTpEKWUc3caA+2E7ETpgupISb9RRXS7OjhXp/rzqR+D8CBJ9rc7uBqZeGoJjBJfl9ii8l+iZWS28BmrnFUr2iFU4P7xj1Gk8HeI6HRFooRZq5zN6j2knLbCrOpZgNsMqsx/bub8Gc/VzE/fuuYJAatHj1w2ZgAR8eelp/3mb6evyY+Oj11jrNpZz+H/3Itoh699H+Jc23Gz0UEaA4kjDjcL+A6HeK45PXsPaHmxYi2NHru9nCeHc96fqBCWnnhNjv3koPXRjA2vxFEgkkT484T74rB/vGbDGK3cLrccQjDoqutzyCmu4zQDVUV1cg1mz7taMJQY90oGnT6y81/Pw1UqO4U0zlUSlHOfywHmFzLZauKea4w3p3LScf9ayyKKdnSqwhlD/D2q0Cce7c8N5LH3RUqCl2o4oel+WtfinRmSebi0G4X6GvxqHturqtYvLbKIPdH0Dl"}
         */
        self.userProfile.username(self.loginRequest.username);
      ///  self.userProfile.authorizationToken(data.authorization_token);
        self.userProfile.authenticated(true);
        // load the navbar and the dashboard, and activate the post-login primary view
        
        var nextView = self.options.postLoginActivatePage; // "dashboard.html";
        for(var i=0; i<DIRECTOR_PAGES.length; i++) {
            var viewDescriptor = DIRECTOR_PAGES[i]; // { href: ...,  target: "#main",  target_tab: "some-id" }
            console.log("post login loading page: %O", viewDescriptor);
            var loadOptions = { into: viewDescriptor.target, tab: null, activate: null };
            if( viewDescriptor.target_tab ) {
                loadOptions.tab = viewDescriptor.target_tab;
            }
            loadOptions.activate = (nextView === viewDescriptor.href); // automatically activate the page specifeid by the "postLoginActivatePage" option
            resourceLoader.loadHTML( viewDescriptor.href , loadOptions );
        }
        

                
    
        
    self.logout = function(logoutFormElement) {
        self.userProfile.authenticated(false);
        self.userProfile.authorizationToken(null);
        console.log("Logout, authenticated=%O, authorizationToken=%O", self.userProfile.authenticated(), self.userProfile.authorizationToken());
        // instead of trying to clear all data, just reload the page now to the index
        window.location = self.options.postLogoutRedirect; // "index.html";
    };
        
    };

}
