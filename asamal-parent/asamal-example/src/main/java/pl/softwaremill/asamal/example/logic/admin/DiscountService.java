package pl.softwaremill.asamal.example.logic.admin;

import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Discount;
import pl.softwaremill.common.cdi.transaction.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Named("discountHelper")
@Transactional

public class DiscountService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private ConfigurationBean configurationBean;

    public Integer getNumberOfUses(Discount discount) {
        return ((Long)entityManager.createQuery("select count(t) from Ticket t where t.invoice.discount = :discount")
                .setParameter("discount", discount).getSingleResult()).intValue();
    }

    public boolean shouldShowLateDiscount(Discount discount) {
        if (discount.getLateDiscount() == null || discount.getLateDiscount().isEmpty()) {
            return false;
        }

        Date lastInvoice = (Date) entityManager.createQuery(
                "select max(i.dateCreated) from Invoice i where i.discount = :discount ")
                .setParameter("discount", discount)
                .getSingleResult();

        Calendar c = Calendar.getInstance();
        c.setTime(lastInvoice);

        c.add(Calendar.MINUTE, configurationBean.getAsInt(Conf.DISCOUNT_LATE_MAX_TIME.toString()));

        return c.after(Calendar.getInstance());
    }

    public void addDiscount(Discount discount) {
        entityManager.persist(discount);
    }

    public List<Discount> getDiscounts() {
        return entityManager.createQuery("select d from Discount d").getResultList();
    }

    public Discount loadDiscount(String discountCode) {
        try {
            return (Discount) entityManager.createQuery("select d from Discount d where d.discountCode = :discountCode")
                    .setParameter("discountCode", discountCode).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
