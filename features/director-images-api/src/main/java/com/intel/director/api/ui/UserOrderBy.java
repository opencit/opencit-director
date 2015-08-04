package com.intel.director.api.ui;

import com.intel.director.api.UserFields;

public class UserOrderBy extends OrderBy {

    UserFields userFields;

    public UserFields getUserFields() {
        return userFields;
    }

    public void setUserFields(UserFields userFields) {
        this.userFields = userFields;
    }

}
