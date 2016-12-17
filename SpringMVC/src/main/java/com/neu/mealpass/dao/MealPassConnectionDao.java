package com.neu.mealpass.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.metal.MetalProgressBarUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neu.mealpass.meal.Meal;
import com.neu.mealpass.meal.MealPass;
import com.neu.mealpass.meal.MealPassOption;
import com.neu.mealpass.meal.Restaurant;
import com.neu.mealpass.meal.RestaurantMeal;
import com.neu.mealpass.user.Address;
import com.neu.mealpass.user.preference.Ingredients;
import com.neu.mealpass.user.preference.MealPortion;
import com.neu.mealpass.user.preference.MealPreference;
import com.neu.mealpass.user.preference.MealTypeEnum;
import com.neu.mealpass.user.preference.UserPreference;

public class MealPassConnectionDao {

	private static final Logger logger = LoggerFactory.getLogger(MealPassConnectionDao.class);
	private static final String TAG = "MealPassConnectionDao ";
	
	/**
	 * Gets the List of MealPass
	 * @param conn
	 * @return
	 */
	public static List<MealPassOption> getMealPassOptions(Connection conn) {
		System.out.println("getMealPassOptions");
		logger.info("getMealPassOptions");
		List<MealPassOption> mealPassOptions = new ArrayList<MealPassOption>();
		String query = "Select * from " + DbConstants.Tables.TABLE_MEALPASS_OPTION;

		ResultSet rs = null;
		try {
			PreparedStatement pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					MealPassOption mealPassOption = new MealPassOption();
					mealPassOption.setMealPassId(rs.getInt(DbConstants.Columns.MEALPASS_ID));
					mealPassOption.setMealPassName(rs.getString(DbConstants.Columns.MEALPASS_NAME));
					mealPassOption.setMealPassDescription(rs.getString(DbConstants.Columns.MEALPASS_DESCRIPTION));
					mealPassOption.setMealPassDurationText(rs.getString(DbConstants.Columns.MEALPASS_DURATION_TEXT));
					mealPassOption.setDurationInMinutes(rs.getString(DbConstants.Columns.DURATION_IN_MINUTES));
					mealPassOption.setImageUrl(rs.getString(DbConstants.Columns.IMAGE_URL));
					mealPassOption.setTotalMeals(rs.getInt(DbConstants.Columns.TOTAL_MEALS));
					mealPassOptions.add(mealPassOption);
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return mealPassOptions;
	}
	/**
	 * Sets the Meal Preference
	 * @param conn
	 * @param userId
	 * @param mealPreference
	 * @return
	 */
	public static boolean setMealPreferences(Connection conn, int userId, MealPreference mealPreference) {
		if (mealPreference != null) {
			if (mealPreference != null) {
				String query = "INSERT INTO " + DbConstants.Tables.TABLE_MEAL_PREFERENCE + " ( "
						+ DbConstants.Columns.MEAL_TYPE + "," + DbConstants.Columns.MEAL_PORTION + ","
						+ DbConstants.Columns.USER_ID + " ) values(?,?,?)";
				String mealType = "";
				String mealPortion = mealPreference.getMealPortion().name();
				List<MealTypeEnum> mealTypeEnums = mealPreference.getMealTypes();
				if (mealTypeEnums != null) {
					for (int i = 0; i < mealTypeEnums.size(); i++) {
						if (i == 0) {
							mealType = mealTypeEnums.get(i).name();
						} else {
							mealType = mealType + "," + mealTypeEnums.get(i).name();
						}
					}
				}

				int row = -1;
				try {
					PreparedStatement pstmt = conn.prepareStatement(query);

					pstmt.setString(1, mealType);
					pstmt.setString(2, mealPortion);
					pstmt.setInt(3, userId);
					row = pstmt.executeUpdate();
					if (row > 0)
						return true;
				} catch (SQLException e) {
					e.printStackTrace();

				}
			}
		}

		return false;
	}
	/**
	 * Sets the Ingredients
	 * @param conn
	 * @param userId
	 * @param ingredients
	 * @return
	 */
	public static boolean setIngredients(Connection conn, int userId, List<Ingredients> ingredients) {

		if (ingredients != null) {
			for (int i = 0; i < ingredients.size(); i++) {
				Ingredients ingredients2 = ingredients.get(0);
				if (ingredients2 != null) {
					String query = "INSERT INTO " + DbConstants.Tables.TABLE_INGREDIENTS + " ( "
							+ DbConstants.Columns.INGREDIENTS_ENUM + "," + DbConstants.Columns.INGREDIENTS_LIKE + ","
							+ DbConstants.Columns.USER_ID + " ) values(?,?,?)";
					int row = -1;
					try {
						PreparedStatement pstmt = conn.prepareStatement(query);

						pstmt.setString(1, ingredients2.getIngredientsEnum().name());
						pstmt.setString(2, ingredients2.getIngredientsLike().name());
						pstmt.setInt(3, userId);
						row = pstmt.executeUpdate();
						if (row > 0)
							return true;
					} catch (SQLException e) {
						e.printStackTrace();

					}
				}
			}
		}

		return false;
	}
	/**
	 * Sets the User Preferences
	 * @param conn
	 * @param userId
	 * @param userPreference
	 * @return
	 */
	public static boolean setPreferences(Connection conn, int userId, UserPreference userPreference) {

		if (userPreference != null) {

			boolean mealPreferenceSet = setMealPreferences(conn, userId, userPreference.getMealPreference());
			System.out.println(TAG + " setPreferences mealPreferenceSet : " + mealPreferenceSet);
			boolean ingredientsPreferenceSet = setIngredients(conn, userId, userPreference.getIngredients());
			System.out.println(TAG + " setPreferences ingredientsPreferenceSet : " + ingredientsPreferenceSet);
			if (mealPreferenceSet || ingredientsPreferenceSet) {

				return true;
			}

		}

		return false;
	}
	/**
	 * Get list based on user preference like meal type, meal portion,ingredients like dislike
	 * @param conn
	 * @param userId
	 * @return
	 */
	public static List<RestaurantMeal> getTodaysRestaurantMeal(Connection conn, int userId) {
		
		System.out.println("getTodaysRestaurantMeal");
		List<RestaurantMeal> todaysRestaurantMeals = new ArrayList<RestaurantMeal>();
		List<MealPassOption> mealPassOptions = getMealPassOptions(conn);
		String query = "Select * from " + DbConstants.Tables.TABLE_RESTAURANTS_MEAL;

		ResultSet rs = null;
		try {
			PreparedStatement pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					RestaurantMeal restaurantMeal = new RestaurantMeal();
					restaurantMeal.setMeal(getMeal(conn,rs.getInt(DbConstants.Columns.ID_MEAL)));
					restaurantMeal.setRestaurant(getRestaurant(conn, rs.getInt(DbConstants.Columns.ID_RESTAURANTS)));
					todaysRestaurantMeals.add(restaurantMeal);
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return todaysRestaurantMeals;
	}
	/**
	 * Returns Meal on passing MealId
	 * @param conn
	 * @param mealId
	 * @return
	 */
	public static Meal getMeal(Connection conn, int mealId){
		Meal meal = null;
		String query = "Select * from " + DbConstants.Tables.TABLE_MEAL + " where "+ DbConstants.Columns.ID_MEAL +"="+mealId;

		ResultSet rs = null;
		try {
			PreparedStatement pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					meal = new Meal();
					meal.setId(mealId);
					meal.setMealName(rs.getString(DbConstants.Columns.MEAL_NAME));
					meal.setMealPortion(MealPortion.valueOf(rs.getString(DbConstants.Columns.MEAL_PORTION)));
					meal.setMealTypeEnum(MealTypeEnum.valueOf(rs.getString(DbConstants.Columns.MEAL_TYPE_ENUM)));
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return meal;
	}
	/**
	 * Gets Restaurant from the restaurant Id
	 * @param conn
	 * @param restaurantId
	 * @return
	 */
	public static Restaurant getRestaurant(Connection conn, int restaurantId){
		Restaurant restaurant = null;
		Address address = new Address();
		String query = "Select * from " + DbConstants.Tables.TABLE_RESTAURANTS + " where "+ DbConstants.Columns.ID_RESTAURANTS +"="+restaurantId;

		ResultSet rs = null;
		try {
			PreparedStatement pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					restaurant = new Restaurant();
					restaurant.setId(restaurantId);
					restaurant.setRestaurantName(rs.getString(DbConstants.Columns.NAME_RESTAURANT));
					restaurant.setRatings(rs.getString(DbConstants.Columns.RATING));
					//restaurant.setAddress(getAddress(conn, rs.getInt(DbConstants.Columns.ADDRESSID)) );
					restaurant.setAddress(address);
					address.setAddress1(rs.getString(DbConstants.Columns.ADDRESS1));
					address.setAddress1(rs.getString(DbConstants.Columns.ADDRESS2));
					address.setCity(rs.getString(DbConstants.Columns.CITY));
					address.setState(rs.getString(DbConstants.Columns.STATE));
					address.setZipCode(rs.getString(DbConstants.Columns.ZIP));
					address.setCountry(rs.getString(DbConstants.Columns.COUNTRY));
					address.setLattitude(Long.parseLong(rs.getString(DbConstants.Columns.LATITUDE)));
					address.setLongitude(Long.parseLong(rs.getString(DbConstants.Columns.LONGITUDE)));
					
					
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return restaurant;
	}
	/**
	 * Gets Address from the Address Id
	 * @param conn
	 * @param addressId
	 * @return
	 */
	public static Address getAddress(Connection conn, int addressId){
		Address address = null;
		String query = "Select * from " + DbConstants.Tables.TABLE_ADDRESS + " where "+ DbConstants.Columns.ADDRESSID +"="+addressId;

		ResultSet rs = null;
		try {
			PreparedStatement pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					address = new Address();
					address.setAddressid(addressId);
					address.setAddress1(rs.getString(DbConstants.Columns.ADDRESS1));
					address.setAddress2(rs.getString(DbConstants.Columns.ADDRESS2));
					address.setCity(rs.getString(DbConstants.Columns.CITY));
					address.setState(rs.getString(DbConstants.Columns.STATE));
					address.setCountry(rs.getString(DbConstants.Columns.COUNTRY));
					address.setZipCode(rs.getString(DbConstants.Columns.ZIP));
					address.setLattitude(rs.getLong(DbConstants.Columns.LATITUDE));
					address.setLongitude(rs.getLong(DbConstants.Columns.LONGITUDE));
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return address;
	}
	/**
	 * 
	 * @param conn
	 * @param mealId
	 * @param userId
	 * @return
	 */
	public static MealPass orderMeal(Connection conn, RestaurantMeal restaurantMeal, String userName) {

		// check remaining meal, enter into order table, subtract meal from
		// restaurant meal count, and subtract user meal remaining

		// populate mealPass and return
		// MealPass mealPass = new MealPass();
		
		MealPass mealPass = null;
		
		if(restaurantMeal.getTotalMeals()>1){
		
		try {
			String query = "UPDATE "+DbConstants.Tables.TABLE_RESTAURANTS_MEAL+" SET "+DbConstants.Columns.TOTAL_MEALS+" = ?, "+DbConstants.Columns.ORDER_DATE +" = ? WHERE +"+DbConstants.Columns.ID_RESTAURANTS +" = ?";
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, (restaurantMeal.getTotalMeals() - 1));
			pstmt.setDate(2, (getCurrentDate()));
			pstmt.setInt(3, restaurantMeal.getRestaurant().getId());
			// execute update SQL statement for restaurant meal count
			pstmt .executeUpdate();
			

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		mealPass = new MealPass();
		try {
			String queryUpdateUser = "UPDATE "+DbConstants.Tables.TABLE_MEALPASS+" SET "+DbConstants.Columns.MEAL_USED+" = ? WHERE +"+DbConstants.Columns.USER_NAME +" = ?";
			PreparedStatement pstmtUpdateUser = conn.prepareStatement(queryUpdateUser);
			pstmtUpdateUser.setInt(1, (mealPass.getMealUsed() + 1));
			pstmtUpdateUser.setString(2, userName);
			// execute update SQL statement for restaurant meal count
			pstmtUpdateUser .executeUpdate();
			

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		}
		

		return mealPass;
	}
	
	private static java.sql.Date getCurrentDate() {
	    java.util.Date today = new java.util.Date();
	    return new java.sql.Date(today.getTime());
	}
	
	public static Boolean userOrderMeal(Connection conn, String userName) {
		
		Boolean userExists = false;
		String query = "Select * from " + DbConstants.Tables.TABLE_RESTAURANTS_MEAL + " where "+ DbConstants.Columns.USER_NAME +"="+userName;
		
		ResultSet rs = null;
		try {
			PreparedStatement pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			if((rs.getDate(DbConstants.Columns.ORDER_DATE)).compareTo(getCurrentDate()) == 0){
				userExists = true;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		return userExists;
	}
	


	
	/**
	 * 
	 * @param conn
	 * @param mealId
	 * @param userId
	 * @return
	 */
	public static MealPass deleteMeal(Connection conn, String mealId, int userId) {

		// delete from order table, increment meal from restaurant meal count,
		// and increment user meal remaining

		// populate mealPass and return
		 /*MealPass mealPass = new MealPass();
		
		try {
		String deleteSQL = "DELETE DBUSER WHERE USER_ID = ?";
		PreparedStatement preparedStatement = conn.prepareStatement(deleteSQL);
		preparedStatement.setInt(1, 1001);
		// execute delete SQL statement
		preparedStatement.executeUpdate();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}*/

		return null;
	}
	
	public static MealPass getUserMealPass(Connection conn, String username){
		MealPass mealPass = null; // return mealpass if exist else null
		
		String query = "SELECT * FROM "+DbConstants.Tables.TABLE_MEALPASS+" WHERE userName = ?";
				
				ResultSet rs = null;
				try {
				PreparedStatement pstmt = conn.prepareStatement(query);

				pstmt.setString(1, username);
				
				rs = pstmt.executeQuery();
				if (rs != null && rs.next()) {
					mealPass = new MealPass();
					mealPass.setPlanName(rs.getString(DbConstants.Columns.PLAN_NAME));
					mealPass.setMealUsed(rs.getInt(Integer.parseInt(DbConstants.Columns.MEAL_USED)));
					mealPass.setMealTotal(rs.getInt(DbConstants.Columns.MEAL_TOTAL));
					mealPass.setStartDate(rs.getString(DbConstants.Columns.MEAL_CYCLE_START_DATE));
					mealPass.setEndDate(rs.getString(DbConstants.Columns.MEAL_CYCLE_END_DATE));
				}

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		return mealPass;
	}
	
	public static RestaurantMeal getUserMeal(Connection connection, String username ){
		RestaurantMeal restaurantMeal = null; // return restaurantMeal if exist else null
		Meal meal = new Meal();
		Restaurant restaurant = new Restaurant();
		Address address = new Address();
			/*Database query is: 
		select * from mealpaldb.Meal a inner join mealpaldb.RestaurantsMeal b 
		on a.idMeal = b.idMeal 
	    inner join mealpaldb.Restaurants c on b.idRestaurants = c.idRestaurants
	    where userName = 'bagankur@gmail.com' and b.orderDate = '2016-12-16';*/
		
		
		String query = "select * from mealpaldb.Meal a inner join mealpaldb.RestaurantsMeal b "+
		"on a.idMeal = b.idMeal inner join mealpaldb.Restaurants c on "+
		"b.idRestaurants = c.idRestaurants where userName = ? and b.orderDate = ?";
		
		ResultSet rs = null;
		try {
		PreparedStatement pstmt = connection.prepareStatement(query);

		pstmt.setString(1, username);
		pstmt.setDate(2, getCurrentDate());
		rs = pstmt.executeQuery();
		if (rs != null && rs.next()) {
			restaurantMeal = new RestaurantMeal();
			restaurantMeal.setMeal(meal);
			restaurantMeal.setRestaurant(restaurant);
			
			meal.setId(rs.getInt(1));
			meal.setMealName(rs.getString(2));
			meal.setMealTypeEnum(MealTypeEnum.valueOf(rs.getString(3)));
			meal.setMealPortion(MealPortion.valueOf(rs.getString(4)));
			
			restaurant.setId(rs.getInt(5));
			restaurant.setAddress(address);
			
			address.setAddress1(rs.getString(12));
			address.setCity(rs.getString(14));
			address.setCountry(rs.getString(17));
			address.setLattitude(Long.parseLong(rs.getString(19)));
			address.setLongitude(Long.parseLong(rs.getString(20)));
			address.setState(rs.getString(15));
			address.setZipCode(rs.getString(16));
			
			restaurant.setRatings(rs.getString(18));
			restaurant.setRestaurantName(rs.getString(11));
			
		}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return restaurantMeal;
	}
}
