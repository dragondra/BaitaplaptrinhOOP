package baitap2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;

public class Controller {

	@FXML
	private MediaView mv;
	@FXML
	private Button playbt, xml;
	@FXML
	private Slider volum, time;
	@FXML
	private TableView tv;
	private TableColumn<?, ?> subtime, realsub;
	private MediaPlayer mp;
	private Media me;

	public void openfile(ActionEvent event) throws IOException {
		FileChooser fc = new FileChooser();
		File seletedFile = fc.showOpenDialog(null);
		String text = seletedFile.getAbsolutePath();
		me = new Media(new File(text).toURI().toString());
		mp = new MediaPlayer(me);
		mv.setMediaPlayer(mp);
		mp.setAutoPlay(true);
		DoubleProperty width = mv.fitWidthProperty();
		DoubleProperty height = mv.fitHeightProperty();

		width.bind(Bindings.selectDouble(mv.sceneProperty(), "width"));
		height.bind(Bindings.selectDouble(mv.sceneProperty(), "height"));
		volum.setValue(mp.getVolume() * 100);
		volum.valueProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable observable) {
				mp.setVolume(volum.getValue() / 100);
			}
		});
		time.valueProperty().addListener(new InvalidationListener() {

			public void invalidated(Observable ov) {
				if (time.isPressed()) {
					mp.seek(mp.getMedia().getDuration().multiply(time.getValue() / 100));
				}

			}

		});
	}

	public void playmusic(ActionEvent event) {
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
	}

	public void importexcel(ActionEvent event) {
		try {
			Object[][] data = null;
			data = new Object[100][1];
			FileChooser fc = new FileChooser();
			File seletedFile = fc.showOpenDialog(null);
			FileInputStream fileToBeRead = new FileInputStream(seletedFile);
			HSSFWorkbook workbook = new HSSFWorkbook(fileToBeRead);
			HSSFSheet sheet = workbook.getSheetAt(0);
			Row row;
			Cell cell = null;
			for (int i = 0; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);

				for (int j = 0; j < 1; j++) {
					String x = row.getCell(0).getStringCellValue();
					data[i][j] = (x);
					System.out.println(data);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("error2");
		}
	}

	public void fast() {
		if (mp.getRate() != 1.0) {
			mp.setRate(1.0);
		} else {
			mp.setRate(2);
		}
	}

	public void slow() {
		System.out.println(mp.getRate());
		if (mp.getRate() != 0.5) {
			mp.setRate(.5);
		} else {
			mp.setRate(1.0);
		}
	}
}
