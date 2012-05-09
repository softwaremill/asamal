package pl.softwaremill.asamal.example.service.user;

import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.service.hash.StringHasher;
import pl.softwaremill.asamal.example.service.user.exception.UserExistsException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.util.Random;

/**
 * Everything about Users
 */
public class UserService implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private StringHasher stringHasher;

    /**
     * Authenticates using given username and password and return the User or null if authentication failed
     */
    public User authenticate(String username, String password) {
        try {
            return (User) entityManager.createQuery(
                    "select u from User u where u.username = :username and u.password = :password")
                    .setParameter("username", username)
                    .setParameter("password", stringHasher.encode(password))
                    .getSingleResult();
        } catch (NoResultException e) {
            // auth failed
            return null;
        }
    }

    public void createNewUser(User user) throws UserExistsException {
        try {
            entityManager.persist(user);
            entityManager.flush();
        } catch (PersistenceException e) {
            throw new UserExistsException("User with login "+user.getUsername()+" already exists.");
        }
    }

    public User loadUser(String login) {
        try {
            return (User) entityManager.createQuery("select u from User u where u.username = :username")
                    .setParameter("username", login)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public String resetPassword(String login) {
        User user = loadUser(login);

        if (user == null) {
            return null;
        }

        String newPass = generateNewPassword(9);

        user.setPassword(stringHasher.encode(newPass));
        user.setPassReset(true);

        entityManager.merge(user);

        return newPass;
    }

    private String generateNewPassword(int length) {
        StringBuffer sb = new StringBuffer();

        Random r = new Random();

        for (int i = 0; i < length; i++) {
            sb.append((char)(97 + r.nextInt(25)));
        }

        return sb.toString();
    }

    public User changePassword(User user, String password) {
        user.setPassword(stringHasher.encode(password));
        user.setPassReset(false);

        return entityManager.merge(user);
    }
}
