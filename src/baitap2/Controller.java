package baitap2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class Controller {
	private static int r = 0;
	@FXML
	private Label ltime, lsub;
	@FXML
	Duration duration;
	@FXML
	private MediaView mv;
	@FXML
	private Button playbt, xml;
	@FXML
	private Slider volume, time;
	@FXML
	private TableView<Table> table;
	@FXML
	private TableColumn<Table, String> time2;
	@FXML
	private TableColumn<Table, String> sub;

	private final ObservableList<Table> data = FXCollections.observableArrayList();

	private MediaPlayer mp;
	private Media me;

	// nut open
	public void openfile(ActionEvent event) throws IOException {
		try {
			// mo file
			FileChooser fc = new FileChooser();
			File seletedFile = fc.showOpenDialog(null);
			String text = seletedFile.getAbsolutePath();
			System.out.println(text);
			// chay file media
			me = new Media(new File(text).toURI().toString());
			mp = new MediaPlayer(me);
			mp.setAutoPlay(true);
			mv.setMediaPlayer(mp);

			time.valueProperty().addListener(new InvalidationListener() {

				public void invalidated(Observable ov) {
					if (time.isPressed()) {
						mp.seek(mp.getMedia().getDuration().multiply(time.getValue() / 100));
					}

				}
			});
			// slider volume
			volume.setValue(mp.getVolume() * 100);
			mp.setOnReady(new Runnable() {
				public void run() {
					duration = mp.getMedia().getDuration();
					updatevalue();
				}
			});
			// thoi gian hien tai
			mp.currentTimeProperty().addListener(new InvalidationListener() {
				public void invalidated(Observable ov) {
					updatevalue();
				}
			});
			// volume hien tai
			volume.valueProperty().addListener(new InvalidationListener() {
				@Override
				public void invalidated(Observable observable) {
					mp.setVolume(volume.getValue() / 100);

				}
			});
		} catch (Exception e) {
			System.out.println("Khong phai file media");
		}
	}

	// nut chay nhac hoac dung lai
	public void playmusic(ActionEvent event) {
		try {
			Status status = mp.getStatus();
			if (status == Status.PLAYING) {
				if (mp.getCurrentTime().greaterThanOrEqualTo(mp.getTotalDuration())) {
					mp.seek(mp.getStartTime());
					mp.play();
				} else {
					mp.pause();
					playbt.setText(">");
				}
			}
			if (status == Status.PAUSED || status == Status.HALTED || status == Status.STOPPED) {
				mp.play();
				playbt.setText("||");
			}
		} catch (Exception e) {
			System.out.println("Khong co file media");
		}
	}

	// nhap file excel va xuat bang
	@SuppressWarnings("resource")
	public void importexcel(ActionEvent event) {
		// chon file excel
		data.clear();
		try {
			FileChooser fc = new FileChooser();
			File seletedFile = fc.showOpenDialog(null);
			FileInputStream fileToBeRead = new FileInputStream(seletedFile);
			HSSFWorkbook workbook = new HSSFWorkbook(fileToBeRead);
			HSSFSheet sheet = workbook.getSheetAt(0);
			Row row;
			// nhap du lieu vao data
			for (int i = 0; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);

				String x = row.getCell(0).getStringCellValue();
				String sub = row.getCell(1).getStringCellValue();
				if (x.length() > 1 && !x.trim().equals(" ")) {
					Table entry = new Table(x, sub);
					data.add(entry);

					System.out.print(x + " - ");
					System.out.println(sub);
				}
			}
			// xuat bang co dung lieu la data
			time2.setCellValueFactory(new PropertyValueFactory<Table, String>("time"));
			sub.setCellValueFactory(new PropertyValueFactory<Table, String>("sub"));
			table.setItems(data);

		} catch (Exception e) {
			System.out.println("Khong phai file excel.xls");
		}
		// dieu kien khi nhan vao bang

		table.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				try {
					if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
						Node node = ((Node) event.getTarget()).getParent();
						TableRow<?> row;
						if (node instanceof TableRow) {
							row = (TableRow<?>) node;
						} else {

							row = (TableRow<?>) node.getParent();
						}
						row.setStyle("-fx-background-color: #808080;");
						Table selectedRow = table.getItems().get(row.getIndex());
						r = row.getIndex();
						String x = selectedRow.getTime();
						Duration drx = new Duration(doi(x));
						mp.seek(drx);
						;
					}
				} catch (Exception e) {
					System.out.println("File excel loi hoac khong co file media");
				}
			}

		});

	}

	// nhanh hon
	public void fast() {
		try {
			if (mp.getRate() != 1.0) {
				mp.setRate(1.0);
			} else {
				mp.setRate(2);
			}
		} catch (Exception e) {
			System.out.println("Khong co file media");
		}
	}

	// cham hon
	public void slow() {
		try {
			System.out.println(mp.getRate());
			if (mp.getRate() != 0.5) {
				mp.setRate(.5);
			} else {
				mp.setRate(1.0);
			}
		} catch (Exception e) {
			System.out.println("Khong co file media");
		}
	}

	// cap nhap gia tri label va slider
	public void updatevalue() {
		Platform.runLater(new Runnable() {
			@SuppressWarnings({ "deprecation" })
			public void run() {
				Duration currentTime = mp.getCurrentTime();
				ltime.setText(formatTime(currentTime, duration));
				time.setValue(currentTime.divide(duration).toMillis() * 100.0);
				lsub.setText(formatSub(currentTime, table));
				table.refresh();
			}
		});
	}

	// khai bao kieu gia tri cua sub
	public static class Table {
		private final SimpleStringProperty time;
		private final SimpleStringProperty sub;

		public Table(String ttime, String ssub) {
			this.time = new SimpleStringProperty(ttime);
			this.sub = new SimpleStringProperty(ssub);
		}

		public String getTime() {
			return time.get();
		}

		public void setTime(String ttime) {
			time.set(ttime);
		}

		public String getSub() {
			return sub.get();
		}

		public void setSub(String ssub) {
			sub.set(ssub);
		}
	}

	// ham doi thoi gian tu string sang int
	protected static int doi(String timeinString) {
		String[] parts = timeinString.split(":");
		String phut = parts[0];
		String giay = parts[1];
		double dphut = Double.parseDouble(phut);
		double dgiay = Double.parseDouble(giay);
		return (int) (dphut * 60000 + dgiay * 1000);
	}

	// ham dinh dang sub
	public static String formatSub(Duration time, TableView<Table> table2) {
		try {
			// table2.setStyle("-fx-background-color: #808080;");
			Duration drx = new Duration(doi(table2.getItems().get(r).getTime()));
			int timese = (int) Math.floor(time.toSeconds());

			// if (timese == drx.toSeconds()) {
			// ObservableList<Table> tempList = table2.getItems();
			// Duration tempdrx = new Duration(doi(tempList.get(r).getTime()));
			// int tempdrxse = (int) Math.floor(tempdrx.toSeconds());
			// if (tempdrxse >= timese) {
			// tempList.get(r).setSub("*" + sub.getSub() + "*");
			// ;
			// }
			// r += 1;
			// return sub.getSub();
			// }
			if (timese >= drx.toSeconds()) {
				ObservableList<Table> tempList = table2.getItems();
				Duration tempdrx = new Duration(doi(tempList.get(r).getTime()));
				int tempdrxse = (int) Math.floor(tempdrx.toSeconds());
				if (tempdrxse >= timese) {
					tempList.get(r).setSub("*" + table2.getItems().get(r).getSub() + "*");
					;
				}
				r += 1;
			}
			return table2.getItems().get(r - 1).getSub();
			// ObservableList<Table> tempList = table2.getItems();
			//// for (int i = 0; i < r; i++) {
			// Duration tempdrx = new Duration(doi(tempList.get(r).getTime()));
			// int tempdrxse = (int) Math.floor(tempdrx.toSeconds());
			// if (tempdrxse >= timese) {
			// tempList.get(r).setSub("*" + x + "*");
			// ;
			// }

			// table2.setRowFactory(param ->
			// {
			// TableRow<Table> row = new TableRow<>();
			// int i =row.getIndex();
			// Duration tempdrx = new Duration(doi(tempList.get(i).getTime()));
			// int tempdrxse = (int) Math.floor(tempdrx.toSeconds());
			// System.out.println("row time "+tempdrxse+" - current
			// time"+timese);
			// if(timese>tempdrxse){
			// row.setStyle("-fx-background-color: red");
			// }
			// else
			// row.setStyle("");
			//
			//
			// return row;
			// });

			// table2..setStyle("-fx-background-color:
			// #131111;-fx-background-size: 100% 40%");
			// ObservableList<Table> tempList = table2.getItems();

		} catch (Exception e) {
		}
		return null;
	}

	// ham dinh dang thoi gian
	private static String formatTime(Duration elapsed, Duration duration) {
		int intElapsed = (int) Math.floor(elapsed.toSeconds());
		int elapsedHours = intElapsed / (60 * 60);
		if (elapsedHours > 0) {
			intElapsed -= elapsedHours * 60 * 60;
		}
		int elapsedMinutes = intElapsed / 60;
		int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

		if (duration.greaterThan(Duration.ZERO)) {
			int intDuration = (int) Math.floor(duration.toSeconds());
			int durationHours = intDuration / (60 * 60);
			if (durationHours > 0) {
				intDuration -= durationHours * 60 * 60;
			}
			int durationMinutes = intDuration / 60;
			int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;
			if (durationHours > 0) {
				return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds,
						durationHours, durationMinutes, durationSeconds);
			} else {
				return String.format("%02d:%02d/%02d:%02d", elapsedMinutes, elapsedSeconds, durationMinutes,
						durationSeconds);
			}
		} else {
			if (elapsedHours > 0) {
				return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
			} else {
				return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
			}
		}
	}
}
