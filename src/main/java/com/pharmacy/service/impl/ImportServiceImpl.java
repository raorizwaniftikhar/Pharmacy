package com.pharmacy.service.impl;

import com.pharmacy.domain.Article;
import com.pharmacy.domain.Pharmacy;
import com.pharmacy.domain.Price;
import com.pharmacy.repository.ArticleRepository;
import com.pharmacy.repository.PharmacyRepository;
import com.pharmacy.repository.search.ArticleSearchRepository;
import com.pharmacy.repository.search.PriceSearchRepository;
import com.pharmacy.service.api.ImportService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Alexander on 14.11.2015.
 */
@Service
@Transactional
public class ImportServiceImpl implements ImportService {

    private final static Logger LOG = LoggerFactory.getLogger(ImportServiceImpl.class);

    @Inject
    private PharmacyRepository pharmacyRepository;
    @Inject
    private ArticleRepository articleRepository;
    @Inject
    private ArticleSearchRepository articleSearchRepository;
    @Inject
    private PriceSearchRepository priceSearchRepository;

    /**
     * This method imports the articles from CSV file and save this into database.
     * The articles ware added to the a pharmacy.
     */
    @Override
    public void importCSVFile() {
        InputStream inputStream = null;
        Long PHARMACY_ID = 1003L;
        Long PHARMACY_ID_TWO = 1004L;
        boolean first = true;
        Pharmacy pharmacy = pharmacyRepository.findOne(PHARMACY_ID);
        Pharmacy pharmacy2 = pharmacyRepository.findOne(PHARMACY_ID_TWO);
        try {
            inputStream = new FileInputStream("C:\\Users\\Alexander\\Dropbox\\Mappe1.csv");
            List<List<String>> csvList = parseCsv(inputStream, ';');
            Article article;
            assert csvList != null;
            for (List<String> attr : csvList) {
                if (first) {
                    article = getArticle(attr, pharmacy);
                    first = false;
                } else {
                    article = getArticle(attr, pharmacy2);
                    first = true;
                }
                Article saveArticle = articleRepository.save(article);
                articleSearchRepository.save(saveArticle);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This method return a article from a line of CSV file.
     *
     * @param attr     is the line of csv file
     * @param pharmacy for assigned the the article.
     * @return article for save
     * @throws ServiceException
     */
    private Article getArticle(List<String> attr, Pharmacy pharmacy) throws ServiceException {
        String articleNumber = attr.get(0);
        Article article;
        if (StringUtils.isBlank(articleNumber)) {
            LOG.debug("number of article {} is empty", articleNumber);
            article = articleRepository.findArticleByArticleNumber(Integer.valueOf(articleNumber));
        } else {
            LOG.debug("number of article {} is not empty", articleNumber);
            article = new Article();
        }

        article.setArticelNumber(Integer.valueOf(articleNumber));
        article.setName(attr.get(2));
        article.setDescription(attr.get(3));
        article.setImageURL(attr.get(7));

        downloadProductImage(attr.get(7), attr.get(2), articleNumber);

//        article.setDeepLink(attr.get(8));
        article.setKeyWords(attr.get(9));
        Assert.notNull(pharmacy);

        Price price;

        double rangeMin = 0.99;
        double rangeMax = 99.99;

        for (int i = 1; i < 10; i++) {
            Random r = new Random();
            double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
            randomValue = Precision.round(randomValue, 2);
            price = new Price();
            price.setPrice(randomValue);
            price.setDiscount(i * 10);
            article.getPrices().add(price);
            price.setArticle(article);
            price.setPharmacy(pharmacy);
        }
        return article;
    }

    private int getDiscount(float suggestedRetailPrice, float price) {
        LOG.trace("Enter getDiscount: suggestedRetailPrice={}, price={}", suggestedRetailPrice, price);
        int result = 0;
        if (suggestedRetailPrice != 0) {
            result = (int) (100 - (price / suggestedRetailPrice) * 100);
        }
        if (result < 0) {
            result = 0;
        }
        LOG.trace("Exit getDiscount: result={}", result);
        return result;
    }

    private float convertStringToFolat(String oldValue) {
        LOG.trace("Enter replaceCommaToPoint: oldValue={}", oldValue);
        float result = 0;
        String newValue = "";
        if (StringUtils.isNotEmpty(oldValue)) {
            if (oldValue.contains(",")) {
                LOG.debug("change comma to point");
                newValue = oldValue.replace(",", ".");
            } else {
                LOG.debug("set old {} to new value", oldValue);
                newValue = oldValue;
            }
            LOG.debug("change value {} to float", newValue);
            result = Float.valueOf(newValue);
        } else {
            LOG.debug("value is empty set 0");
        }
        LOG.trace("Exit replaceCommaToPoint: newValue={}", newValue);
        return result;
    }


    /**
     * CSV content parser. Convert an InputStream with the CSV contents to a
     * two-dimensional List of Strings representing the rows and columns of the
     * CSV. Each CSV record is expected to be separated by the specified CSV
     * field separator.
     *
     * @param inputStream  The InputStream with the CSV contents.
     * @param csvSeparator The CSV field separator to be used.
     * @return A two-dimensional List of Strings representing the rows and
     * columns of the CSV.
     */
    private List<List<String>> parseCsv(InputStream inputStream, char csvSeparator) throws ServiceException {
        BufferedReader csvReader = null;
        try {
            List<List<String>> csvList = new ArrayList<>();
            csvReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String csvRecord;
            boolean fistLine = true;
            while ((csvRecord = csvReader.readLine()) != null) {
                if (fistLine) {
                    LOG.debug("ignore fist line of csv file becouse this is a header");
                    fistLine = false;
                } else {
                    LOG.trace("csvRecord={}", csvRecord);
                    csvList.add(parseCsvRecord(csvRecord, csvSeparator));
                }

            }
            return csvList;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (csvReader != null) {
                    csvReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private void downloadProductImage(String imageURL, String productName, String id) {
        Image image = null;
        try {
            URL url = new URL(imageURL);
            InputStream in = new BufferedInputStream(url.openStream());
            OutputStream out = new BufferedOutputStream(new FileOutputStream(id + "_" + productName + ".jpg"));
            for ( int i; (i = in.read()) != -1; ) {
                out.write(i);
            }
            in.close();
            out.close();
        } catch (IOException ex) {
            LOG.warn("Image could be not download");
        }
    }

    /**
     * CSV record parser. Convert a CSV record to a List of Strings representing
     * the fields of the CSV record. The CSV record is expected to be separated
     * by the specified CSV field separator.
     *
     * @param record       The CSV record.
     * @param csvSeparator The CSV field separator to be used.
     * @return A List of Strings representing the fields of each CSV record.
     */
    private List<String> parseCsvRecord(String record, char csvSeparator) {
        LOG.trace("Enter parseCsvRecord: record={}, csvSeparator={}", record, csvSeparator);
        // Prepare.
        boolean quoted = false;
        StringBuilder fieldBuilder = new StringBuilder();
        List<String> fields = new ArrayList<>();

        // Process fields.
        for (int i = 0; i < record.length(); i++) {
            char c = record.charAt(i);
            fieldBuilder.append(c);

            if (c == '"') {
                quoted = !quoted; // Detect nested quotes.
            }

            if ((!quoted && c == csvSeparator) // The separator ..
                    || i + 1 == record.length()) // .. or, the end of record.
            {
                String field = fieldBuilder.toString() // Obtain the field, ..
                        .replaceAll(csvSeparator + "$", "") // .. trim ending separator, ..
                        .replaceAll("^\"|\"$", "") // .. trim surrounding quotes, ..
                        .replace("\"\"", "\""); // .. and un-escape quotes.
                fields.add(field.trim()); // Add field to List.
                fieldBuilder = new StringBuilder(); // Reset.
            }
        }
        LOG.trace("Exit parseCsvRecord: fields={}", fields);
        return fields;
    }
}
