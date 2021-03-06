package com.zerokol.rentamovie.activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.zerokol.rentamovie.R;
import com.zerokol.rentamovie.dao.MovieDAO;
import com.zerokol.rentamovie.models.Movie;
import com.zerokol.rentamovie.utils.RentAMovieHelper;

public class MoviesActivity extends ActionBarActivity {
	private MovieDAO movieDataAccessObject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movies);

		movieDataAccessObject = new MovieDAO(this);

		RequestMoviesAsyncTask requestMoviesTask = new RequestMoviesAsyncTask(
				this);
		requestMoviesTask.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.movie, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class RequestMoviesAsyncTask extends
			AsyncTask<Context, Integer, JSONArray> {
		private ProgressDialog progressDialog;

		public RequestMoviesAsyncTask(Context context) {
			progressDialog = new ProgressDialog(context);
		}

		protected void onPreExecute() {
			progressDialog.setTitle(getResources().getString(
					R.string.request_title));
			progressDialog.setMessage(getResources().getString(
					R.string.request_message));

			progressDialog.show();
		}

		@Override
		protected JSONArray doInBackground(Context... params) {
			String url = RentAMovieHelper.DOMAIN + "/service/movies.json";

			JSONArray jsonArray = null;

			try {
				String response = RentAMovieHelper.makeGETRequest(url);

				if (response != null) {
					jsonArray = new JSONArray(response);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jsonArray;
		}

		@Override
		protected void onPostExecute(JSONArray jsonArray) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}

			try {
				if (jsonArray != null) {
					movieDataAccessObject.open();
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject movieJson = jsonArray.getJSONObject(i);
						Movie movie = movieDataAccessObject
								.selectById(movieJson.getInt(MovieDAO.TABLE_ID));

						if (movie == null) {
							movie = new Movie();

							movie.setId(movieJson.getInt(MovieDAO.TABLE_ID));
							movie.setName(movieJson
									.getString(MovieDAO.TABLE_NAME));
							movie.setDescription(movieJson
									.getString(MovieDAO.TABLE_DESCRIPTION));
							movie.setImage(movieJson
									.getJSONObject(MovieDAO.TABLE_IMAGE)
									.getJSONObject("medium").getString("url"));
							movie.setQuantity(movieJson
									.getInt(MovieDAO.TABLE_QUANTITY));
							movie.setCreatedAt(RentAMovieHelper.convertStringDateToDate(movieJson
									.getString(MovieDAO.TABLE_CREATED_AT)));
							movie.setUpdatedAt(RentAMovieHelper.convertStringDateToDate(movieJson
									.getString(MovieDAO.TABLE_UPDATED_AT)));

							movieDataAccessObject.create(movie);
						} else {
							if (movie
									.getUpdatedAt()
									.compareTo(
											RentAMovieHelper
													.convertStringDateToDate(movieJson
															.getString(MovieDAO.TABLE_UPDATED_AT))) != 0) {
								movie.setName(movieJson
										.getString(MovieDAO.TABLE_NAME));
								movie.setDescription(movieJson
										.getString(MovieDAO.TABLE_DESCRIPTION));
								movie.setImage(movieJson
										.getJSONObject(MovieDAO.TABLE_IMAGE)
										.getJSONObject("medium")
										.getString("url"));
								movie.setQuantity(movieJson
										.getInt(MovieDAO.TABLE_QUANTITY));
								movie.setUpdatedAt(RentAMovieHelper.convertStringDateToDate(movieJson
										.getString(MovieDAO.TABLE_UPDATED_AT)));

								movieDataAccessObject.update(movie);
							}
						}
					}
					movieDataAccessObject.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				super.onPostExecute(jsonArray);
			}
		}
	}
}
