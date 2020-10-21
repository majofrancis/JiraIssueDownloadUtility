package com.jira.utility.api;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginStage extends Stage {

	private static String jqlRestUrl;
	private static Integer finaltotalIssues;
	private static String username;
	private static String password;
	private static ArrayList<FieldNamesWithId> list = new ArrayList<FieldNamesWithId>();
	private static final String jqlRestUrlSuffix = "/rest/api/2/search?jql=";
	private static final String customFieldsRestUrlSuffix = "/rest/api/2/customFields?maxResults=500";

	BorderPane borderPane = new BorderPane();

	HBox hBox = new HBox();

	// Implementing Nodes for GridPane
	Label lableJqlURL = new Label("JIRA JQL Page URL");
	final TextField textBoxJQLURL = new TextField();
	Label labelUserName = new Label("Username");
	final TextField textBoxUserName = new TextField();
	Label labelPassword = new Label("Password");
	final PasswordField passwordField = new PasswordField();
	Button buttonLogin = new Button("Login");
	final Label labelMessage = new Label();

	// Adding text and DropShadow effect to it
	Text titleText = new Text("JIRA Issue Download Utility");

	LoginStage() {

		borderPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
		borderPane.setPadding(new Insets(10, 50, 50, 50));

		hBox.setPadding(new Insets(20, 20, 20, 30));

		// create a background fill
		BackgroundFill background_fill = new BackgroundFill(Color.CADETBLUE, CornerRadii.EMPTY, Insets.EMPTY);
		// create Background
		Background background = new Background(background_fill);
		// set background
		hBox.setBackground(background);

		// Adding GridPane
		GridPane gridPane = new GridPane();
		gridPane.setPadding(new Insets(20, 20, 20, 20));
		gridPane.setHgap(5);
		gridPane.setVgap(5);

		// Adding Nodes to GridPane layout
		gridPane.add(lableJqlURL, 0, 0);
		gridPane.add(textBoxJQLURL, 1, 0);
		gridPane.add(labelUserName, 0, 1);
		gridPane.add(textBoxUserName, 1, 1);
		gridPane.add(labelPassword, 0, 2);
		gridPane.add(passwordField, 1, 2);
		gridPane.add(buttonLogin, 3, 2);
		gridPane.add(labelMessage, 1, 3);
		
		// Reflection for gridPane
		Reflection reflection = new Reflection();
		reflection.setFraction(0.7f);

		titleText.setFont(Font.font("Courier New", FontWeight.BOLD, 28));
		titleText.setFill(Color.TURQUOISE);

		// Adding text to HBox
		hBox.getChildren().add(titleText);

		// Add ID's to Nodes
		borderPane.setId("borderPane");
		gridPane.setId("root");
		buttonLogin.setId("buttonLogin");
		titleText.setId("text");

		// Add HBox and GridPane layout to BorderPane Layout
		borderPane.setTop(hBox);
		borderPane.setCenter(gridPane);

		// Adding BorderPane to the scene and loading CSS
		Scene scene = new Scene(borderPane);

		this.setScene(scene);
		this.show();

		buttonLogin.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {

			    String jiraInstanceBaseUrl = null;
				String jql = null;
				
				try {
					String userEnteredUrl = textBoxJQLURL.getText().toString();
					
			//		userEnteredUrl = "https://jira.com/issues/?jql=issuetype%20%3D%20Test%20AND%20creator%20in%20(majofran)%20ORDER%20BY%20created%20DESC";
			//      OR https://jira.com/issues/?filter=77300#					
					
					if( userEnteredUrl.indexOf(".com/") != -1) {		// Normal URL 
						jiraInstanceBaseUrl = userEnteredUrl.substring(0, userEnteredUrl.indexOf(".com/") + 4 );
					}
					
					else if( userEnteredUrl.indexOf(".com:") != -1) {   // Direct server URL with 4 digit port
						jiraInstanceBaseUrl = userEnteredUrl.substring(0, userEnteredUrl.indexOf(".com:") + 9 );
					}
					
					if(userEnteredUrl.indexOf("jql=") != -1) {		// JQL URL provided
						jql = userEnteredUrl.substring(userEnteredUrl.indexOf("jql=") + 4);
					}
					
					else if (userEnteredUrl.indexOf("filter=")!= -1)	// Filter URL provided
					{
						jql = userEnteredUrl.substring(userEnteredUrl.indexOf("filter="));
					}
					
					if ( jiraInstanceBaseUrl == null || jql == null)
					{
						labelMessage.setText("Invalid URL. Please provide the URL of Search Issues Page");
						labelMessage.setTextFill(Color.RED);
					}
					
					
					jqlRestUrl =  jiraInstanceBaseUrl + jqlRestUrlSuffix + jql;
					
					System.out.println(" jqlUrl " + jqlRestUrl);
	
					username = textBoxUserName.getText().toString();
					password =  passwordField.getText().toString();
	
					int responseCode = getResponseSetFinaltotalIssues(jqlRestUrl, username, password); // this will get the total number of issues in JQL
																									
					if (responseCode == 200) {
						if (finaltotalIssues == 0)
						{
							labelMessage.setText("There are no Matching Issues Found in JIRA.");
							return;
						}
						getCustomfieldsJira( (jiraInstanceBaseUrl + customFieldsRestUrlSuffix) , username, password);
						new FieldStage(list, jqlRestUrl, username, password, finaltotalIssues);
						labelMessage.setText("Connection Success : Please select the fields for the Report");
						labelMessage.setTextFill(Color.GREEN);
						
						((Stage) labelMessage.getScene().getWindow()).close();					

					} else {
						labelMessage.setText(" Response Code is : " + responseCode + "\n 400:Bad Request, 401:Unauthorized, "
								+ "\n403:Forbidden, 404:Not Found");
						labelMessage.setTextFill(Color.RED);
					}
					textBoxUserName.setText("");
					passwordField.setText("");
				
				} catch (Exception e) {
					labelMessage.setText("Invalid URL. Please provide the URL of Search Issues Page");
					labelMessage.setTextFill(Color.RED);
					e.printStackTrace();
				}
			}
		});
	}

	private static void getCustomfieldsJira(String url, String username, String password) {
		
		System.out.println("customfields URl " + url);
		JiraUtilityResponse fieldResponse = new JiraUtilityRequest(url, username, password).call();
		if (fieldResponse.getResponseCode() == 200) {
			try {
				
				list.add(new FieldNamesWithId("Key","Key", false));
				list.add(new FieldNamesWithId("summary","Summary", false));
				list.add(new FieldNamesWithId("Description","Description", false));
				list.add(new FieldNamesWithId("Status","Status", false));
				
				list.add(new FieldNamesWithId("project","Project", false));
				list.add(new FieldNamesWithId("projectcategory","Project Category", false)); 
				// To get Project category, MUST select Project column as well.

				list.add(new FieldNamesWithId("Issuetype","Issuetype", false));
				list.add(new FieldNamesWithId("Reporter","Reporter", false));
				list.add(new FieldNamesWithId("Assignee","Assignee", false));
				
				list.add(new FieldNamesWithId("Priority","Priority", false));
				list.add(new FieldNamesWithId("Creator","Creator", false));
				list.add(new FieldNamesWithId("Resolution","Resolution", false));
				list.add(new FieldNamesWithId("Votes","Votes", false));
				list.add(new FieldNamesWithId("Watches","Watches", false)); 
				
				list.add(new FieldNamesWithId("Updated","Updated", false));
				list.add(new FieldNamesWithId("Created","Created", false));

				final JSONObject jsonFieldObject = new JSONObject(fieldResponse.getResponseBody());

				final JSONArray issuefieldArray = jsonFieldObject.getJSONArray("values");
				
				for (int i = 0; i < issuefieldArray.length(); i++) {
					final JSONObject objectfieldlopcal = issuefieldArray.getJSONObject(i);

					if (objectfieldlopcal.get("name").toString() != null
							&& objectfieldlopcal.get("id").toString() != null ) {

						if (objectfieldlopcal.get("name").toString().equalsIgnoreCase("Development") ||
								objectfieldlopcal.get("name").toString().equalsIgnoreCase("Epic Colour") ||
										objectfieldlopcal.get("name").toString().equalsIgnoreCase("Epic Status") ||
												objectfieldlopcal.get("name").toString().equalsIgnoreCase("Rank") || 
														objectfieldlopcal.get("name").toString().equalsIgnoreCase("Rank (Obsolete)") )
								{

							continue; 
						}
						
						list.add(new FieldNamesWithId(objectfieldlopcal.get("id").toString(), objectfieldlopcal.get("name").toString(), false));
					}
				}

			} catch (JSONException e) {

				e.printStackTrace();
			}
		}
	}

	private static int getResponseSetFinaltotalIssues(String url, String username, String password) {

		JiraUtilityResponse respon = new JiraUtilityRequest(url, username, password).call();
		if (respon.getResponseCode() == 200) {
			try {
				final JSONObject jsonObject = new JSONObject(respon.getResponseBody());
				finaltotalIssues = (Integer) jsonObject.get("total");

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return respon.getResponseCode();
	}

}