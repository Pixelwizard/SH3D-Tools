package com.hal9000.sh3d.stereo_pov_plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.plugin.PluginAction.Property;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.tools.URLContent;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;

public class SH3D_StereoPOVPlugin extends Plugin {
	PluginAction[] pluginactions;
	
	@Override
	public PluginAction[] getActions() {
		getUserPreferences().addPropertyChangeListener(UserPreferences.Property.LANGUAGE, new LanguageChangeListener(this));
		
		pluginactions=new PluginAction [] {new SH3D_StereoscopicRenderPlugin_Action(this.getHomeController().getHomeController3D().getView())};
		return pluginactions;
	}
	
	private static class LanguageChangeListener implements PropertyChangeListener {
	    private WeakReference<SH3D_StereoPOVPlugin> plugin;

	    public LanguageChangeListener(SH3D_StereoPOVPlugin plugin) {
	        this.plugin = new WeakReference<SH3D_StereoPOVPlugin>(plugin);
	    }
	    
		public void propertyChange(PropertyChangeEvent event) {
			SH3D_StereoPOVPlugin plugin=this.plugin.get();
			if (plugin==null){
				((UserPreferences)event.getSource()).removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
			}
			else {
				plugin.pluginactions[0].putPropertyValue(Property.NAME,ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(plugin.getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.MENU_OPTION"));
				if (((SH3D_StereoscopicRenderPlugin_Action)(plugin.pluginactions[0])).mi_dialogo.dialog != null){
					if (((SH3D_StereoscopicRenderPlugin_Action)(plugin.pluginactions[0])).mi_dialogo.dialog.isShowing()){
						((SH3D_StereoscopicRenderPlugin_Action)(plugin.pluginactions[0])).mi_dialogo.updateLanguage();
					}
				}
			}
		}
	}
	
	public class SH3D_StereoscopicRenderPlugin_Action extends PluginAction {
		View mi_view;
		MainDialog mi_dialogo;
		
		public SH3D_StereoscopicRenderPlugin_Action(View view) {
			mi_view=view;
			putPropertyValue(Property.NAME,ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.MENU_OPTION"));
			
			putPropertyValue(Property.MENU, getUserPreferences().getLocalizedString(HomePane.class, "VIEW_3D_MENU.Name"));
			putPropertyValue(Property.SMALL_ICON, new URLContent(com.hal9000.sh3d.stereo_pov_plugin.SH3D_StereoPOVPlugin.class.getResource("3d_glasses.png")));
			setEnabled(true);
		}
		
		@Override
		public void execute() {
			mi_dialogo=new MainDialog();	

			mi_dialogo.displayView(mi_view);
		}
	}

	
	
	public class MainDialog extends JPanel implements DialogView, ActionListener {		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3875999917093329221L;

		JDialog dialog;
		JSpinner sp_eyes_separation;
		JTextField tf_name;
		JButton btn_ok;
		JButton btn_cancel;
		JLabel lbl_eyes;
		JLabel lbl_name;
		JTextArea ta_note;

		public void displayView(View parentView) {
			boolean is_observer=(getHome().getCamera() instanceof ObserverCamera);
			
			if (!is_observer){
				getHomeController().getHomeController3D().viewFromObserver();
			}
			
			lbl_eyes = new JLabel ("");
			lbl_name = new JLabel ("");
			lbl_name.setAlignmentX(LEFT_ALIGNMENT);
			
			ta_note = new JTextArea ("");
			ta_note.setWrapStyleWord(true);
			ta_note.setLineWrap(true);
			ta_note.setEditable(false);
			ta_note.setFocusable(false);
			ta_note.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
			ta_note.setBackground(lbl_eyes.getBackground());
			Font fnt_nota=lbl_eyes.getFont().deriveFont(Font.ITALIC, lbl_eyes.getFont().getSize2D()*0.85f);
			ta_note.setFont(fnt_nota);
			ta_note.setAlignmentX(LEFT_ALIGNMENT);
			
			sp_eyes_separation=new JSpinner();
			sp_eyes_separation.setValue(6.5f);
			float eyes_separation;
			if (getHome().getVisualProperty("SH3D_StereoPOVPlugin_eyes_separation")!=null){
				eyes_separation=(Float) getHome().getVisualProperty("SH3D_StereoPOVPlugin_eyes_separation");
			}
			else {
				eyes_separation=6.5f;
			}
			sp_eyes_separation.setModel(new SpinnerNumberModel(eyes_separation, 5, 10, 0.1));
			JSpinner.NumberEditor ne_eyes_separation=(JSpinner.NumberEditor)sp_eyes_separation.getEditor();
			ne_eyes_separation.getFormat().setMinimumFractionDigits(1);
			ne_eyes_separation.getFormat().setMaximumFractionDigits(1);
			ne_eyes_separation.getFormat().setDecimalSeparatorAlwaysShown(true);
			JFormattedTextField ftf_eyes_separation=ne_eyes_separation.getTextField();
			ftf_eyes_separation.setColumns(3);
			ftf_eyes_separation.setHorizontalAlignment(JTextField.RIGHT);
			
			tf_name=new JTextField("",30);
			tf_name.setHorizontalAlignment(JTextField.LEFT);
			tf_name.setAlignmentX(LEFT_ALIGNMENT);
			tf_name.setMaximumSize(new Dimension(300,24));
			tf_name.setMinimumSize(new Dimension(300,24));
			
			JOptionPane main_panel=new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new Object[] {}, null);
			
			JPanel eyes_panel=new JPanel();
			eyes_panel.setAlignmentX(LEFT_ALIGNMENT);
			FlowLayout eyes_layout=new FlowLayout(FlowLayout.LEFT);
			eyes_layout.setHgap(0);
			eyes_panel.setLayout(eyes_layout);
			eyes_panel.add(lbl_eyes);
			eyes_panel.add(sp_eyes_separation);
			
			Box box_buttons=Box.createHorizontalBox();
			box_buttons.setAlignmentX(LEFT_ALIGNMENT);
			btn_ok=new JButton("Crear");			
			btn_cancel=new JButton("Cancelar");
			btn_ok.addActionListener(this);
			btn_cancel.addActionListener(this);
			box_buttons.add(Box.createHorizontalGlue());
			box_buttons.add(btn_ok);
			box_buttons.add(Box.createHorizontalStrut(50));
			box_buttons.add(btn_cancel);
			box_buttons.add(Box.createHorizontalGlue());
			
			Box box_main=Box.createVerticalBox();			
			box_main.setAlignmentX(LEFT_ALIGNMENT);
			box_main.add(eyes_panel);
			box_main.add(Box.createVerticalStrut(10));
			box_main.add(lbl_name);
			box_main.add(tf_name);
			box_main.add(Box.createVerticalStrut(10));
			box_main.add(ta_note);
			box_main.add(Box.createVerticalStrut(30));
			box_main.add(box_buttons);
			
			BorderLayout layout=new BorderLayout();
			main_panel.setLayout(layout);
			main_panel.add(box_main,BorderLayout.NORTH);			
						
			dialog=main_panel.createDialog(SwingUtilities.getRootPane((Component)parentView),"");
			dialog.setModal(false);
			dialog.setMinimumSize(new Dimension(320,0));
			dialog.setMaximumSize(new Dimension(320,600));
			tf_name.requestFocus();
			updateLanguage();
			dialog.setVisible(true);
			dialog.pack();
		}

		
		public void updateLanguage(){
			dialog.setTitle(ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.MENU_OPTION"));
			lbl_eyes.setText(ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.EYES_SEPARATION")+" (cm): ");
			lbl_name.setText(ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.POV_NAME")+"\u00B9:");
			ta_note.setText("\u00B9 "+ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.NOTE"));
			btn_ok.setText(ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.CREATE"));
			btn_cancel.setText(ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.CANCEL"));
		}
		
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==btn_ok){
				if (tf_name.getText().trim().length()==0){
					String msg=ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.NO_NAME_ERROR");
					String title=ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.ERROR");
					JOptionPane.showMessageDialog(dialog, msg,title,JOptionPane.ERROR_MESSAGE);
				}
				else {
					crearPuntosDeVista(tf_name.getText().trim());
				}
			}
			else if (e.getSource()==btn_cancel) {
				dialog.dispose();
			}
		}
		
		public void crearPuntosDeVista (String name) {
			Camera current_camera=getHome().getCamera().clone();
			boolean is_observer=(current_camera instanceof ObserverCamera);
			ObserverCamera current_observer_camera=getHome().getObserverCamera().clone();
			ObserverCamera camera_l=current_observer_camera.clone();
			ObserverCamera camera_r=current_observer_camera.clone();
			
			if (!is_observer){
				getHomeController().getHomeController3D().viewFromObserver();
			}
			
			float angle=current_observer_camera.getYaw();
			float eyes_separation=Double.valueOf((Double) sp_eyes_separation.getValue()).floatValue();
			String name_l=name+" [Stereo-L]";
			String name_r=name+" [Stereo-R]";
			
			camera_l.setX((current_observer_camera.getX()+Double.valueOf(Math.cos(angle)).floatValue()*eyes_separation/2f));
			camera_l.setY((current_observer_camera.getY()+Double.valueOf(Math.sin(angle)).floatValue()*eyes_separation/2f));
			
			camera_r.setX((current_observer_camera.getX()-Double.valueOf(Math.cos(angle)).floatValue()*eyes_separation/2f));
			camera_r.setY((current_observer_camera.getY()-Double.valueOf(Math.sin(angle)).floatValue()*eyes_separation/2f));
						
			getHome().setCamera(camera_l);
			getHomeController().getHomeController3D().storeCamera(name_l);
			
			getHome().setCamera(camera_r);
			getHomeController().getHomeController3D().storeCamera(name_r);
			
			getHome().setCamera(current_observer_camera);
			
			getHome().setVisualProperty("SH3D_StereoPOVPlugin_eyes_separation", eyes_separation);
			
			String msg=ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.DONE_MESSAGE");
			String title=ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.DONE");
			String opt_close=ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.CLOSE");
			String opt_creation=ResourceBundle.getBundle("com.hal9000.sh3d.stereo_pov_plugin.package",new Locale(getUserPreferences().getLanguage())).getString("SH3D_StereoPOVPlugin.GO_TO_CREATION");
			int option=JOptionPane.showOptionDialog(dialog, msg+":\n\n"+name_l+"\n\n"+name_r, title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[] {opt_close, opt_creation}, null);
			
			dialog.dispose();
			if (option==1){
				getHomeController().createPhotos();
			}
		}
	}
}
