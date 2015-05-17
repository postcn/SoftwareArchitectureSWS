import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class PictureManager {
	public static final File PICTURE_DIRECTORY = new File("Pictures");
	private List<String> pictures;
	

	public PictureManager() throws Exception {
		if (!PICTURE_DIRECTORY.exists()) {
			if (!PICTURE_DIRECTORY.mkdir()) {
				throw new Exception("Picture Directory not created.");
			}
		}
		if (PICTURE_DIRECTORY.isFile()) {
			throw new Exception("Picture Directory cannot be a file.");
		}
		refreshFileList();
	}
	
	private void refreshFileList() {
		pictures = new ArrayList<String>();
		for (File f : PICTURE_DIRECTORY.listFiles()) {
			if (f.isFile()) {
				pictures.add(f.getName());
			}
		}
	}
	
	public List<String> getAllNames() {
		return pictures;
	}
	
	public List<String> getAllNames(String prefix) {
		LinkedList<String> retStrings = new LinkedList<String>();
		for (String s: pictures) {
			if (s.startsWith(prefix)) {
				retStrings.add(s);
			}
		}
		return retStrings;
	}
	
	public void deleteAll() {
		for (File f : PICTURE_DIRECTORY.listFiles()) {
			f.delete();
		}
	}
	
	public void deleteAllMatching(String type) {
		for (File f : PICTURE_DIRECTORY.listFiles()) {
			if (f.getName().endsWith(type)) {
				f.delete();
			}
		}
	}
	
	public File getPicture(String pictureName) {
		if (pictures.contains(pictureName)) {
			return new File(PICTURE_DIRECTORY.getAbsolutePath() + "/" + pictureName);
		}
		else {
			return null;
		}
	}
	
	public boolean deletePicture(String pictureName) {
		File pictureFile = getPicture(pictureName);
		if (pictureFile != null) {
			return pictureFile.delete();
		}
		return false;
	}
	
	public boolean createPicture(String pictureName, byte[] contents) {
		File pictureFile = getPicture(pictureName);
		if (pictureFile == null) {
			try {
				FileOutputStream stream = new FileOutputStream(PICTURE_DIRECTORY.getAbsolutePath() + "/" + pictureName);
				stream.write(contents);
				stream.close();
				pictures.add(pictureName);
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean editPicture(String pictureName, byte[] contents) {
		File pictureFile = getPicture(pictureName);
		if (pictureFile != null) {
			try {
				pictureFile.delete();
				FileOutputStream stream = new FileOutputStream(pictureFile.getAbsolutePath());
				stream.write(contents);
				stream.close();
				pictures.add(pictureName);
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
