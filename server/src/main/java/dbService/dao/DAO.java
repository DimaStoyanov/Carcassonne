package dbService.dao;

import dbService.dataSets.AbstractDataSet;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 * Created by Dmitrii Stoianov
 */


public class DAO {
    private final Session session;

    public DAO(Session session) {
        this.session = session;
    }

    public long addNote(AbstractDataSet dataSet) {
        return (Long) session.save(dataSet);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractDataSet> T getNote(Class dataSetClass, String key, String value) {
        Criteria criteria = session.createCriteria(dataSetClass);
        return (T) criteria.add(
                Restrictions.eq(key, value)).uniqueResult();
    }

    public void deleteNote(AbstractDataSet dataSet) {
        session.delete(dataSet);
    }

    public void updateNote(AbstractDataSet dataSet) {
        session.update(dataSet);
    }
}

