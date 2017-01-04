package validator.admin.AdminUserService;

import dao.AuthCredentialDao;
import dao.UserInfDao;
import dao.UserRoleDao;
import entity.AuthCredential;
import entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Created by mi on 1/4/17.
 */
@Service
public class CreateAdminUserValidator implements Validator {
    @Autowired
    AuthCredentialDao authCredentialDao;

    @Autowired
    UserInfDao userInfDao;

    @Autowired
    UserRoleDao userRoleDao;

    @Override
    public void validate(Object obj, Errors errors) {
        CreateAdminUserForm createAdminUserForm = (CreateAdminUserForm)obj;


        UserRole userRole = userRoleDao.getById(createAdminUserForm.getRoleId());
        if(userRole==null) {
            errors.rejectValue("roleId", "No role found");
        }

        AuthCredential authCredential  = authCredentialDao.getByUserName(createAdminUserForm.getUserName());
        if(authCredential!=null){
            errors.rejectValue("userName", "User name already taken");

            if(authCredential.getUserInf()!=null && authCredential.getUserInf().getEmail().equals(createAdminUserForm.getEmail())){
                String fullName = authCredential.getUserInf().getFirstName()+" "+authCredential.getUserInf().getLastName();
                errors.rejectValue("email", "Email name already been used by "+fullName);
            }
        }



    }
    @Override
    public boolean supports(Class<?> aClass) {
        return CreateAdminUserForm.class.equals(aClass);
    }
}
