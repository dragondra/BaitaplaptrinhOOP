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
			r = 0;
			try {
				mp.pause();
			} catch (Exception e) {
			}

			FileChooser fc = new FileChooser();
			File seletedFile = fc.showOpenDialog(null);
			String text = seletedFile.getAbsolutePath();
			System.out.println(text);

			// chay file media
			me = new Media(new File(text).toURI().toString());
			mp = new MediaPlayer(me);
			mp.setAutoPlay(true);
			mv.setMediaPlayer(mp);
			// dieu kien cua slider thoi gian
			time.valueProperty().addListener(new InvalidationListener() {
				public void invalidated(Observable ov) {
					// neu bi nhan vao mot vi tri nao do thi se lay thoi gian
					// tai vi tri do va chay tiep
					if (time.isPressed()) {
						mp.seek(mp.getMedia().getDuration().multiply(time.getValue() / 100));
					}

				}
			});
			// lay thoi gian hien tai
			mp.setOnReady(new Runnable() {
				public void run() {
					duration = mp.getMedia().getDuration();
					// updatevalue();
				}
			});
			// update tat ca gia tri cua lable va slider theo thoi gian troi qua
			mp.currentTimeProperty().addListener(new InvalidationListener() {
				public void invalidated(Observable ov) {
					updatevalue();
				}
			});
			// slider volume cho volume ban dau la 100%
			volume.setValue(mp.getVolume() * 100);

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
			if (status == Status.PLAYING) { // neu media dang chay
				// neu thoi gian troi qua > = thoi gian hien tai
				if (mp.getCurrentTime().greaterThanOrEqualTo(mp.getTotalDuration())) {
					mp.seek(mp.getStartTime());
					mp.play();
				} else {
					mp.pause();
					playbt.setText(">");
				}
			}
			// neu media tam ngung || bi bat dung lai || dung lai
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

		try {
			FileChooser fc = new FileChooser();
			File seletedFile = fc.showOpenDialog(null);
			FileInputStream fileToBeRead = new FileInputStream(seletedFile);
			HSSFWorkbook workbook = new HSSFWorkbook(fileToBeRead);
			HSSFSheet sheet = workbook.getSheetAt(0);
			Row row;
			data.clear(); // xoa du lieu cua bang truoc khi nhap
			// nhap du lieu vao data
			for (int i = 0; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);

				String x = row.getCell(0).getStringCellValue();
				String sub = row.getCell(1).getStringCellValue();
				if (!x.trim().equals(" ") && x.length() > 1) { // loai bo cac
																// truong hop
																// cac cot co
																// gia tri rong
					Table entry = new Table(x, sub);
					data.add(entry);
					System.out.println(x + " - " + sub);
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
						// row.setStyle("-fx-background-color: #808080;");
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
	// neu toc do hien tai bang 1 thi cho no = 2 neu khac 1 thi cho no lai bang
	// 1
	public void fast() {
		try {
			if (mp.getRate() != 2.0) {
				mp.setRate(2.0);
			} else {
				mp.setRate(1);
			}
		} catch (Exception e) {
			System.out.println("Khong co file media");
		}
	}

	// cham hon
	// neu toc do hien tai bang 0.5 thi cho no = 1 neu khac 0.5 thi cho no bang
	// 0.5
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
		Platform.runLater(new Runnable() { // dung de cap nhap GUI tu cac ham
			@SuppressWarnings({ "deprecation" })
			public void run() {
				// lay thoi gian da troi qua
				Duration currentTime = mp.getCurrentTime();
				// dinh dang lable thoi gian
				ltime.setText(formatTime(currentTime, duration));
				// System.out.println(formatTime(currentTime, duration));
				// dinh dang lai slider thoi gian
				time.setValue(currentTime.divide(duration).toMillis() * 100.0);
				// dinh dang lai lable sub
				lsub.setText(formatSub(currentTime, table));
				// System.out.println(formatSub(currentTime, table));
				// lam moi bang
				table.refresh();
			}
		});
	}

	// ham khai bao kieu gia tri cua sub
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
		return (int) (dphut * 60000 + dgiay * 1000); // tra lai gia tri ms
	}

	// ham dinh dang sub
	public static String formatSub(Duration time, TableView<Table> table2) {
		try {
			ObservableList<Table> tempList = table2.getItems();
			Duration tempdrx = new Duration(doi(tempList.get(r).getTime()));
			if (Math.floor(time.toSeconds()) >= tempdrx.toSeconds()) {
				// if (tempdrx.toSeconds() >= Math.floor(time.toSeconds())) {
				try {
					tempList.get(r - 1).setSub((tempList.get(r - 1).getSub()).replaceAll("-", ""));
					tempList.get(r).setSub((tempList.get(r).getSub()).replaceAll("-", ""));
					tempList.get(r + 1).setSub((tempList.get(r + 1).getSub()).replaceAll("-", ""));
					tempList.get(r + 2).setSub((tempList.get(r + 2).getSub()).replaceAll("-", ""));
					tempList.get(r + 3).setSub((tempList.get(r + 3).getSub()).replaceAll("-", ""));
				} catch (Exception e) {
				}
				tempList.get(r).setSub("-" + tempList.get(r).getSub() + "-");
				// }

				r += 1;
			}
			// if (Math.floor(time.toSeconds()) >= tempdrx.toSeconds()
			// && Math.floor(time.toSeconds()) >= (tempdrx.toSeconds() + 2)) {
			// return null;
			// }
			return tempList.get(r - 1).getSub();
		} catch (Exception e) {
		}
		return null;
	}

	// ham dinh dang thoi gian
	// nhan 2 gia tri la thoi gian troi qua va thoi gian hien tai
	private static String formatTime(Duration elapsed, Duration duration) {
		int intElapsed = (int) Math.floor(elapsed.toSeconds()); // lam tron thoi
																// gian hien tai
																// thanh
		int elapsedHours = intElapsed / (60 * 60); // doi tu giay sang gio
		// neu gio troi qua > 0 thi so giay se bi dinh dang lai
		if (elapsedHours > 0) {
			intElapsed -= elapsedHours * 60 * 60;
		}
		int elapsedMinutes = intElapsed / 60; // dinh dang sang phut
		int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;// tinh
																						// lai
																						// giay

		if (duration.greaterThan(Duration.ZERO)) {
			int intDuration = (int) Math.floor(duration.toSeconds());
			int durationHours = intDuration / (60 * 60);
			if (durationHours > 0) {
				intDuration -= durationHours * 60 * 60;
			}
			int durationMinutes = intDuration / 60;
			int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;
			// neu thoi gian hien tai > 1 gio
			if (durationHours > 0) {
				// tra lai gia tri gio phut giay
				return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds,
						durationHours, durationMinutes, durationSeconds);
			} else {
				// tra lai gia tri phut giay
				return String.format("%02d:%02d/%02d:%02d", elapsedMinutes, elapsedSeconds, durationMinutes,
						durationSeconds);
			}
		} else {
			if (elapsedHours > 0) {
				// System.out.println(String.format("%d:%02d:%02d",
				// elapsedHours, elapsedMinutes, elapsedSeconds));
				return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
			} else {
				// System.out.println(String.format("%02d:%02d", elapsedMinutes,
				// elapsedSeconds));
				return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
			}
		}
	}
}
