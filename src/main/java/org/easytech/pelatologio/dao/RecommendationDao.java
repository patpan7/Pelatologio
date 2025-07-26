package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Recommendation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public interface RecommendationDao {

    List<Recommendation> getRecommendations();

    void saveRecommendation(Recommendation recommendation);

    void updateRecommendation(Recommendation recommendation);

    void deleteRecommendation(int id);
}
