package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.enums.Lang;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import edu.sumdu.tss.elephant.helper.utils.ValidatorHelper;
import edu.sumdu.tss.elephant.model.DbUserService;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.model.UserService;
import io.javalin.Javalin;
import io.javalin.core.util.JavalinLogger;
import io.javalin.http.Context;

public class ProfileController extends AbstractController {

    public static final String BASIC_PAGE = "/profile";

    public ProfileController(Javalin app) {
        super(app);
    }

    public static void show(Context context) {
        context.render("/velocity/profile/show.vm", currentModel(context));
    }

    public static void language(Context context) {
        User user = currentUser(context);
        var lang = context.queryParam("lang");
        user.setLanguage(Lang.byValue(lang).toString());
        UserService.save(user);
        context.redirect(BASIC_PAGE);
    }

    public static void resetDbPassword(Context context) {
        User user = currentUser(context);
        String newPassword = context.formParam("db-password");
        JavalinLogger.info(user.toString());
        if(newPassword != null && ValidatorHelper.isValidDBPassword(newPassword)){
            user.setDbPassword(context.formParam("db-password"));
            UserService.save(user);
            DbUserService.dbUserPasswordReset(user.getUsername(), user.getDbPassword());
            context.sessionAttribute(Keys.INFO_KEY, "DB user password was changed");
        } else{
            context.sessionAttribute(Keys.ERROR_KEY, "DB user password does not match pattern [a-z0-9]{10}");
        }
        JavalinLogger.info(user.toString());
        context.redirect(BASIC_PAGE);
    }

    public static void resetWebPassword(Context context) {
        User user = currentUser(context);
        String newPassword = context.formParam("web-password");
        if (newPassword != null && ValidatorHelper.isValidPassword(newPassword)) {
            user.password(newPassword);
            UserService.save(user);
            context.sessionAttribute(Keys.INFO_KEY, "Web user password was changed");
        } else {
            context.sessionAttribute(Keys.ERROR_KEY, "Password should be at least 8 symbols,"
                    + " with at least 1 digit, 1 uppercase letter and 1 non alpha-num symbol");
        }
        context.redirect(BASIC_PAGE);
    }

    public static void resetApiPassword(Context context) {
        User user = currentUser(context);
        //TODO add password validation
        user.setPrivateKey(StringUtils.randomAlphaString(User.API_KEY_SIZE));
        user.setPublicKey(StringUtils.randomAlphaString(User.API_KEY_SIZE));
        UserService.save(user);
        context.sessionAttribute(Keys.INFO_KEY, "API keys was reset successful");
        context.redirect(BASIC_PAGE);
    }

    public static void upgradeUser(Context context) {
        User user = currentUser(context);
        user.setRole(UserRole.valueOf(context.formParam("role")).getValue());
        UserService.save(user);
        context.sessionAttribute(Keys.INFO_KEY, "Role has been changed");
        context.redirect(BASIC_PAGE);
    }

    public static void removeSelf(Context context) {
        User user = currentUser(context);
        DbUserService.dropUser(user.getUsername());
        //TODO: delete all user-specific files
        //TODO: logout
        //TODO: remove web-user from DB
        context.redirect("/");
    }

    public void register(Javalin app) {
        app.get(BASIC_PAGE + "/lang", ProfileController::language, UserRole.AUTHED);
        app.post(BASIC_PAGE + "/reset-password", ProfileController::resetWebPassword, UserRole.AUTHED);
        app.post(BASIC_PAGE + "/reset-db", ProfileController::resetDbPassword, UserRole.AUTHED);
        app.post(BASIC_PAGE + "/reset-api", ProfileController::resetApiPassword, UserRole.AUTHED);
        app.post(BASIC_PAGE + "/upgrade", ProfileController::upgradeUser, UserRole.AUTHED);
        app.post(BASIC_PAGE + "/remove-self", ProfileController::removeSelf, UserRole.AUTHED);
        app.get(BASIC_PAGE, ProfileController::show, UserRole.AUTHED);
    }

}
