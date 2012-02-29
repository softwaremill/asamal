package pl.softwaremill.asamal.example.service.user;

import pl.softwaremill.asamal.example.model.User;
import pl.softwaremill.asamal.example.service.hash.StringHasher;
import pl.softwaremill.common.cdi.transaction.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.io.Serializable;

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
    @Transactional
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

    @Transactional
    public void createNewUser(User user) {
        entityManager.persist(user);
    }
}
