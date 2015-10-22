package com.jfixby.tools.bleed.gemserk;

import java.io.IOException;

import com.gemserk.tools.bleed.Magic;
import com.jfixby.cmns.api.filesystem.File;
import com.jfixby.cmns.api.filesystem.LocalFileSystem;
import com.jfixby.cmns.api.log.L;
import com.jfixby.cmns.api.path.ChildrenList;
import com.jfixby.cmns.api.path.FileFilter;
import com.jfixby.tools.bleed.api.TextureBleedProcessor;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public class GemserkProcessorImpl implements TextureBleedProcessor {

	private File in_folder;

	public GemserkProcessorImpl(TextureBleedSpecs gemserk_processor_specs) {

		in_folder = gemserk_processor_specs.getInputFolder();
		if (!in_folder.isFolder()) {
			throw new Error(in_folder + " is not a folder.");
		}

	}

	@Override
	public void process() throws IOException {

		ChildrenList files_to_serve = in_folder.listChildren().filter(
				png_filter);

		L.d("---------------GEMSERK-----------------");
		if (true) {

			for (int i = 0; i < files_to_serve.size(); i++) {
				File atlas_data_file = (files_to_serve.getElementAt(i));

				java.io.File file_to_process = LocalFileSystem
						.toJavaFile(atlas_data_file);
				L.d("Gemserk-processing file", file_to_process);
				try {
					Magic.process(file_to_process, file_to_process);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				L.d("--------------------");
			}

		}

	}

	private FileFilter png_filter = new FileFilter() {
		@Override
		public boolean fits(File child) {
			return child.getName().toLowerCase().endsWith(".png");
		}
	};
}
