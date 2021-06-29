package com.pharmacy.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Price.
 */
@Entity
@Table(name = "price")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "price")
public class Price implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "suggested_retail_price")
    private Float suggestedRetailPrice;

    @Column(name = "extra_shipping_suffix")
    private String extraShippingSuffix;

    @Column(name = "discount")
    private Integer discount;

    @Column(name = "price")
    private Double price;

    @OneToOne(cascade = CascadeType.ALL)
    @Field(type = FieldType.Object, index = FieldIndex.not_analyzed)
    private Pharmacy pharmacy;

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    private Article article;

    @Column(name = "deep_link", length = 1000)
    private String deepLink;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getSuggestedRetailPrice() {
        return suggestedRetailPrice;
    }

    public void setSuggestedRetailPrice(Float suggestedRetailPrice) {
        this.suggestedRetailPrice = suggestedRetailPrice;
    }

    public String getExtraShippingSuffix() {
        return extraShippingSuffix;
    }

    public void setExtraShippingSuffix(String extraShippingSuffix) {
        this.extraShippingSuffix = extraShippingSuffix;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Price price = (Price) o;

        return Objects.equals(id, price.id);

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Price{" +
                "id=" + id +
                ", suggestedRetailPrice='" + suggestedRetailPrice + "'" +
                ", extraShippingSuffix='" + extraShippingSuffix + "'" +
                ", discount='" + discount + "'" +
                ", price='" + price + "'" +
                ", deepLink='" + deepLink + "'" +
                '}';
    }
}
