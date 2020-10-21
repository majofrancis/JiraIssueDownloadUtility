package com.jira.utility.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

public class FieldStage extends Stage {

	private static String maxResults = "&maxResults=";
	private static int maxResultsnumber = 1000;
	private static String urlsuffix = "&startAt=";
	//private static String fields = "&fields="; // "&fields=summary,status";
	private static int urlsuffixTotalPages;
	private static FileOutputStream fos;
	private static FileInputStream fis;
	private static final String defaultMessage = "No Fields Selected.\n\nPlease Note:\ni) Project Category can only be selected along with Project Column";

	Label x = new Label("Second stage");
	BorderPane brdrPane = new BorderPane();
	HBox topSection = new HBox();
	HBox bottomSection = new HBox();
	HBox responseSection = new HBox();
	Pane centerSection = new Pane();
	Pane centerSectionRight = new Pane();
	ListView<FieldNamesWithId> listFields = new ListView<>();
	DirectoryChooser directoryChooser = new DirectoryChooser();

	FieldStage(ArrayList<FieldNamesWithId> list, String jqlurl, String userId, String pwd,
			Integer finaltotalIssuecount) {

		topSection.setPadding(new Insets(3));
		topSection.setSpacing(5);
		topSection.setAlignment(Pos.CENTER);

		listFields.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		
		Callback<FieldNamesWithId, ObservableValue<Boolean>> getProperty = new Callback<FieldNamesWithId, ObservableValue<Boolean>>() {
			@Override
			public BooleanProperty call(FieldNamesWithId temp) {

				return temp.selectedProperty();
			}
		};
		
		Callback<ListView<FieldNamesWithId>, ListCell<FieldNamesWithId>> forListView = CheckBoxListCell.forListView(getProperty);
		listFields.setCellFactory(forListView);
		

		CheckBox buttonselectNone = new CheckBox(" Select None ");
		buttonselectNone.setId("buttonSelectNone");
		buttonselectNone.setPadding(new Insets(3,300,3,3));
		bottomSection.getChildren().add(buttonselectNone);

		Button buttonIssueDetails = new Button(" Export Issue Details ");
		buttonIssueDetails.setId("buttonIssueDetails");
		bottomSection.getChildren().add(buttonIssueDetails);

		Button buttonSaveMappings = new Button(" Save Field Mappings ");
		buttonSaveMappings.setId("buttonSaveMappings");
		bottomSection.getChildren().add(buttonSaveMappings);

		Button buttonLoadMappings = new Button(" Load Field Mappings ");
		buttonLoadMappings.setId("buttonLoadMappings");
		bottomSection.getChildren().add(buttonLoadMappings);

		bottomSection.setPadding(new Insets(3));
		bottomSection.setAlignment(Pos.BOTTOM_LEFT);

		Label labelSelectedItem = new Label(defaultMessage);
				
		labelSelectedItem.setPadding(new Insets(5));

		ScrollPane scrollerRight = new ScrollPane(centerSectionRight);
		centerSectionRight.getChildren().add(labelSelectedItem);

		final Label labelMessage = new Label();
		responseSection.getChildren().add(labelMessage);
		responseSection.setPadding(new Insets(3));
		responseSection.setAlignment(Pos.CENTER);

		listFields.getItems().addAll(list);

		listFields.setPrefHeight(listFields.getItems().size() * 26);
		
		ScrollPane scrollerLeft = new ScrollPane(centerSection);

		centerSection.getChildren().add(listFields);
		
		brdrPane.setTop(topSection);
		brdrPane.setLeft(scrollerLeft);
		brdrPane.setCenter(scrollerRight);
		brdrPane.setBottom(bottomSection);
		brdrPane.setRight(labelMessage);
		Scene scene2 = new Scene(brdrPane);

		this.setMaximized(true);
		this.setScene(scene2);
		this.show();
		
		// Whenever Field list is clicked - update the 'Fields selected' Pane
		listFields.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {

				boolean toggleCheckBox = listFields.getSelectionModel().getSelectedItem().getSelected();
				
				listFields.getSelectionModel().getSelectedItem().setSelected( ! toggleCheckBox );

				labelSelectedItem.setText(getItems());
			}
		});
		
		// Whenever any Check box is checked/ unchecked - update the 'Fields selected' Pane
		listFields.getItems().forEach(task -> task.selectedProperty().addListener((observable, wasSelected, isSelected) -> {

		    labelSelectedItem.setText(getItems());
		    
		}));
		
		//Check Box to Deselect all Field selections at once
		buttonselectNone.setOnAction(new EventHandler() {
			@Override
			public void handle(Event event) {
				
				for (FieldNamesWithId tempObj : listFields.getItems()) {
					tempObj.setSelected(false);
	            }
			}
		});
		
		// Export Issue Details in to Excel file
		buttonIssueDetails.setOnAction(new EventHandler() {

			@Override
			public void handle(Event event) {
				
				final ObservableList<FieldNamesWithId> selectedFields 
				=  listFields.getItems().stream().filter (a -> a.getSelected()==true).collect(Collectors.toCollection(FXCollections::observableArrayList));				

				try {
					System.out.println(" Total Issue Count : " + finaltotalIssuecount);
					
					labelMessage.setText("Please be patient while we work on your Report.  \n "
							+ "Depending on the report, it may take few seconds to several minutes.   \n "
							+ "You will see a SUCCESS prompt once the export task is Complete.     " );
					labelMessage.setTextFill(Color.DARKORANGE);

					if ( selectedFields.isEmpty())
					{
						labelMessage.setText("No fields Selected. Please select the fields to include in the Report ");
						labelMessage.setTextFill(Color.RED);						
					}
					
					else
					{
						generateExcel(jqlurl, userId, pwd, finaltotalIssuecount, selectedFields);
						
						
						labelMessage.setText( "Excel file downloaded Successfully  !!          ");
						labelMessage.setTextFill(Color.GREEN);
					}

				} catch (Exception e) {
					
					e.printStackTrace();
					labelMessage.setText("CSV is not generated. Please contact JIRA administrator !!       \n" + e.getMessage());
					labelMessage.setTextFill(Color.RED);
				}
			}
		});

		// Save the Field Mappings selected by the User into User's Local machine
		buttonSaveMappings.setOnAction(new EventHandler() {

			@Override
			public void handle(Event event) {
				
				final ObservableList<FieldNamesWithId> selectedFields 
				=  listFields.getItems().stream().filter (a -> a.getSelected()==true).collect(Collectors.toCollection(FXCollections::observableArrayList));
				
				try {
					
					if (!selectedFields.isEmpty()) {
						exportFieldMappings(selectedFields);
 
						labelMessage.setText("Field Mappings exported Successfully !!            " );
						labelMessage.setTextFill(Color.GREEN);
					} else {
						labelMessage.setText("Error Exporting Field Mappings !!                 ");
						labelMessage.setTextFill(Color.RED);
					}

				} catch (Exception e) {
					e.printStackTrace();
					labelMessage.setText("Error Exporting Field Mappings !!                     ");
					labelMessage.setTextFill(Color.RED);
				}
			}
		});

		// Allow user to Load existing Field Mappings file from his Local machine
		buttonLoadMappings.setOnAction(new EventHandler() {
			@Override
			public void handle(Event event) {
				try {

					final ObservableList<FieldNamesWithId> selectedFields;

					selectedFields = loadFieldMappings();

					if (!selectedFields.isEmpty()) {
						labelMessage.setText("Field Mappings Loaded Successfully !!           ");

						labelMessage.setTextFill(Color.GREEN);

						listFields.getSelectionModel().clearSelection();
						for (FieldNamesWithId tempObj : listFields.getItems()) {
							tempObj.setSelected(false);
			            }						

						for (FieldNamesWithId tempObj : selectedFields) {

							for (FieldNamesWithId temp2 : listFields.getItems()) {
								if (temp2.getId().equals(tempObj.getId())) {
									listFields.getSelectionModel().select(temp2);
									temp2.setSelected(true);									
								}
							}
						}
					} else {
						throw new IOException();
					}

				} catch (Exception e) {
					e.printStackTrace();
					labelMessage.setText("Error Reading Properties file !!           ");
					labelMessage.setTextFill(Color.RED);
				}
			}
		});

	}

	// Gets Checked items from the Fields List
	private String getItems() {

		ObservableList<FieldNamesWithId> selectedFieldsTemp 
		=  listFields.getItems().stream().filter (a -> a.getSelected()==true).collect(Collectors.toCollection(FXCollections::observableArrayList));

		if (selectedFieldsTemp.size() == 0) {
			return defaultMessage;
		}
		if (selectedFieldsTemp.size() == 1) {
			return "Selected Items : \n " + selectedFieldsTemp.get(0);
		}
		String displayText = "Selected Items are : \n ";
		for (FieldNamesWithId fieldsTemp : selectedFieldsTemp) {
			displayText += fieldsTemp.toString() + " \n ";
		}
		return displayText;
	}

	private void exportFieldMappings(ObservableList<FieldNamesWithId> selectedFields) throws IOException {

		Properties properties = new Properties();

		for (FieldNamesWithId field : selectedFields) {
			properties.put(field.getId(), field.getName());
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save");
		fileChooser.setInitialFileName("FieldMappings.properties");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Properties File", "*.properties"));

		File propertiesFile = fileChooser.showSaveDialog(getScene().getWindow());

		if (propertiesFile != null) {
			fos = new FileOutputStream(propertiesFile);

			properties.store(fos, null);

			fos.flush();
			fos.close();
		}
	}

	private ObservableList<FieldNamesWithId> loadFieldMappings() throws IOException {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Properties File", "*.properties*"));

		File propertiesFile = fileChooser.showOpenDialog(getScene().getWindow());

		List<FieldNamesWithId> tempList = new ArrayList<FieldNamesWithId>();

		fis = new FileInputStream(propertiesFile);

		Properties properties = new Properties();
		properties.load(fis);

		for (String key : properties.stringPropertyNames()) {
			tempList.add(new FieldNamesWithId(key, properties.get(key).toString(), false));
		}

		ObservableList<FieldNamesWithId> selectedFields = FXCollections.observableArrayList(tempList);

		return selectedFields;
	}

	private void generateExcel(String url, String username, String password, Integer totalIssueCount,
			ObservableList<FieldNamesWithId> selectedFields) throws Exception {

		urlsuffixTotalPages = (totalIssueCount % maxResultsnumber == 0) ? (totalIssueCount / maxResultsnumber)
				: ((totalIssueCount / maxResultsnumber) + 1);

		System.out.println(" maxResultsnumber : " + maxResultsnumber);
		System.out.println(" urlsuffixTotalPages : " + urlsuffixTotalPages);

		TreeMap<String, String> tempSelectedFieldsMap = new TreeMap<String, String>();
		String fields = "&fields="; // "&fields=summary,status";
		
		for (FieldNamesWithId temp : selectedFields) {
			fields = fields + temp.getId().toLowerCase() + ",";
			tempSelectedFieldsMap.put(temp.getId().toLowerCase(), temp.getName());
		}

		// XLSX code
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save");
		fileChooser.setInitialFileName("JIRA Issues Export.xlsx");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Excel File", "*.xlsx"));
		
		File excelFile = fileChooser.showSaveDialog(getScene().getWindow());
		
		if(excelFile != null ) {

		fos = new FileOutputStream(excelFile);
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("JIRA");
		
		 XSSFCellStyle cellStyle = workbook.createCellStyle();
		  cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		  cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		  
		  Font font = workbook.createFont();
		  font.setBold(true);
		  cellStyle.setFont(font);
		
		int firstrow = 0;
		int rowCount = 2;

		for (Integer c = 0; c < urlsuffixTotalPages; c++) {
			String urlList = url + maxResults + maxResultsnumber + urlsuffix + (c * maxResultsnumber) + fields;
			System.out.println(" URL Hit : " + urlList);
			
			JiraUtilityResponse response = new JiraUtilityRequest(urlList, username, password).call();

				final JSONObject jsonObject = new JSONObject(response.getResponseBody());
				final JSONArray issuesArray = jsonObject.getJSONArray("issues");

				// System.out.println("MJ issuesArray " + issuesArray.toString());

				System.out.println("MJ issuesArray.length() " + issuesArray.length());

				for (int i = 0; i < issuesArray.length(); i++) {
					final JSONObject issuesJsonObject = issuesArray.getJSONObject(i);
					
					TreeMap<String, String> finalCustomFieldValues = new TreeMap<String, String>();

					Iterator<String> keyslocals = issuesJsonObject.keys();
					while (keyslocals.hasNext()) {

						String keyslocal = keyslocals.next();

						if (keyslocal.equals("fields")) {
							final JSONObject fieldsJsonObject = (JSONObject) issuesJsonObject.get(keyslocal);

							Iterator<String> fieldsKeys = fieldsJsonObject.keys();
							while (fieldsKeys.hasNext()) {
								String keysfield = fieldsKeys.next();

								finalCustomFieldValues.put(tempSelectedFieldsMap.get(keysfield),
										subFieldValues(fieldsJsonObject, keysfield));
			
							if ( keysfield.equals("project") && tempSelectedFieldsMap.containsKey("projectcategory")) {

								finalCustomFieldValues.put("Project Category",
										getProjectCategory(fieldsJsonObject, "project"));
							}
							
						}
						}

						else if (keyslocal.equals("key")) {
							finalCustomFieldValues.put("Issue Key", issuesJsonObject.getString(keyslocal));
						}

					}

					if (firstrow == 0) {

						firstrow++;
						Row firstRow = sheet.createRow(0);
						Row secondRow = sheet.createRow(1);
						int cellCount = 0;
						for (Map.Entry<String, String> entry : finalCustomFieldValues.entrySet()) {
							Cell cellR1 = firstRow.createCell(cellCount);
							Cell cellR2 = secondRow.createCell(cellCount);

							cellR1.setCellValue(entry.getKey());
							cellR1.setCellStyle(cellStyle);
							cellR2.setCellValue(entry.getValue());
							cellCount++;
						}
					} else {

						Row row = sheet.createRow(rowCount++);
						int cellCount = 0;
						for (Map.Entry<String, String> entry : finalCustomFieldValues.entrySet()) {
							Cell cellRi = row.createCell(cellCount);
							cellRi.setCellValue(entry.getValue());
							cellCount++;
						}
					}
				}
		}

		workbook.write(fos);
		workbook.close();
		fos.flush();
		fos.close();
		}
		
		else {
			throw new IOException();
		}
	}
	
	
	
	private String getProjectCategory(JSONObject issueFieldsObject, String keysfield) throws JSONException {
		final JSONObject objectissueSubfields;
		String subFieldValue = "";
		
		if (issueFieldsObject.get(keysfield).toString() != null && issueFieldsObject.get(keysfield).toString() != ""
				&& issueFieldsObject.get(keysfield).toString() != "null"
				&& issueFieldsObject.get(keysfield).toString().startsWith("{") ) {

				if (!(issueFieldsObject.get(keysfield).toString().equals("{}")
						&& issueFieldsObject.get(keysfield).toString().equals("[]"))) {
				
					objectissueSubfields = issueFieldsObject.getJSONObject(keysfield);

					if (objectissueSubfields.has("projectCategory"))  {

						final JSONObject projectCategory = new JSONObject(objectissueSubfields.get("projectCategory").toString());
						subFieldValue = (String) projectCategory.get("name") ;
					} 
				}
		}
		return subFieldValue.toString();
	}

	private String subFieldValues(JSONObject issueFieldsObject, String keysfield) throws JSONException {
		final JSONObject objectissueSubfields;
		String subFieldValue = "";

			if (issueFieldsObject.get(keysfield).toString() != null && issueFieldsObject.get(keysfield).toString() != ""
					&& issueFieldsObject.get(keysfield).toString() != "null"
					&& issueFieldsObject.get(keysfield).toString().startsWith("{") && !keysfield.equals("description") ) {

					if (!(issueFieldsObject.get(keysfield).toString().equals("{}")
							&& issueFieldsObject.get(keysfield).toString().equals("[]"))) {
						
						objectissueSubfields = issueFieldsObject.getJSONObject(keysfield);
						Iterator<String> keysSubfields = objectissueSubfields.keys();
						
						while (keysSubfields.hasNext()) {
							keysSubfields.next();

							if (keysfield.equals("status")) {
								subFieldValue = objectissueSubfields.get("name").toString();
								break;
							} else if (keysfield.equals("resolution")) {
								subFieldValue = objectissueSubfields.get("name").toString();
								break;
							} else if (keysfield.equals("customfield_18900")) {  // Code Base
								subFieldValue = objectissueSubfields.get("formatted").toString();
								break;
							} else if (keysfield.equals("customfield_18901")) {  // Project_Name
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("customfield_11134")) {		// Quality Impact
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("assignee")) {
								subFieldValue = objectissueSubfields.get("name").toString();
								break;
							} else if (keysfield.equals("customfield_11145")) {		// Severity
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("reporter")) {
								subFieldValue = objectissueSubfields.get("name").toString();
								break;
							} else if (keysfield.equals("customfield_11135")) {		// Quality Dimension
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("customfield_11137")) {		// Reproducible
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("progress")) {
								subFieldValue = objectissueSubfields.get("progress").toString();
								break;
							} else if (keysfield.equals("votes")) {
								subFieldValue = objectissueSubfields.get("votes").toString();
								break;
							} else if (keysfield.equals("issuetype")) {
								subFieldValue = objectissueSubfields.get("name").toString();
								break;
							} else if (keysfield.equals("project")) {
								subFieldValue = objectissueSubfields.get("key").toString();  // key or name ??
								break;
							}
							
							else if (keysfield.equals("customfield_11116")) {		// Found Area/Sub Area

								subFieldValue = objectissueSubfields.get("value").toString();
								
								if ( objectissueSubfields.get("child") != null) {

									final JSONObject subAreaJson = new JSONObject(objectissueSubfields.get("child").toString());
									String subArea = (String) subAreaJson.get("value") ;
									subFieldValue = subFieldValue + ":" + subArea;
								}
								
								break;
							} 					
							else if (keysfield.equals("customfield_14509")) {		// Risk
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("watches")) {
								subFieldValue = objectissueSubfields.get("watchCount").toString();
								break;
							} else if (keysfield.equals("customfield_11108")) {		// Defect Type
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("priority")) {
								subFieldValue = objectissueSubfields.get("name").toString();
								break;
							} else if (keysfield.equals("creator")) {
								subFieldValue = objectissueSubfields.get("name").toString();
								break;
							} else if (keysfield.equals("aggregateprogress")) {
								subFieldValue = objectissueSubfields.get("progress").toString();
								break;
							} else if (keysfield.equals("customfield_18700")) {		// Device Type
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("customfield_14441")) {		// Language Translations
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("customfield_14321")) {		// Currency-Global
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("customfield_11604")) {		// Proposed Resolution
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("customfield_18800")) { // Automated Tests
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("customfield_17301")) { // Main Component
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else if (keysfield.equals("customfield_15900")) { // Regression
								subFieldValue = objectissueSubfields.get("value").toString();
								break;
							} else {
								
								subFieldValue = issueFieldsObject.get(keysfield).toString()
										.replace(System.getProperty("line.separator"), "");
							}

						}
					}

			} else {

				subFieldValue = issueFieldsObject.get(keysfield).toString().length() > 32760 ? 
						issueFieldsObject.get(keysfield).toString().substring(0, 32760) :
							issueFieldsObject.get(keysfield).toString() ;
						// Excel cell supports max 32767 chars in a Cell.
						
			}

		return subFieldValue.toString();
	}

}