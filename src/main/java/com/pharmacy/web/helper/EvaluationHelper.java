/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pharmacy.web.helper;


import com.pharmacy.domain.Evaluation;

import java.util.List;

/**
 *
 * @author Alexandr
 */
public class EvaluationHelper {
    
    public int calculateRating(List<Evaluation> evaluations) {
        float result = 0;
        for (Evaluation evaluation : evaluations) {
            result = result + evaluation.getPoints();
        }
        return Math.round(result / evaluations.size());
    }
}
