package info.jtrac.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serializable;

/**
 * Created by ncrappe on 8/09/2015.
 */
@Entity
public class StoredSearch implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String query;

    public StoredSearch() {
    }

    public StoredSearch(Long id, String name, String query) {
        this.id = id;
        this.query = query;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getId() {
        if(id == null){
            return new Long(0);
        }
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
