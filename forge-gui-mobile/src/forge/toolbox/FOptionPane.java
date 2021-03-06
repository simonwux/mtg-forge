package forge.toolbox;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.math.Vector2;

import forge.Forge;
import forge.Graphics;
import forge.assets.FSkinFont;
import forge.assets.FImage;
import forge.assets.FSkinImage;
import forge.card.CardRenderer;
import forge.card.CardZoom;
import forge.card.CardRenderer.CardStackPosition;
import forge.screens.match.views.VPrompt;
import forge.toolbox.FEvent.*;
import forge.util.Callback;
import forge.util.Utils;
import forge.game.card.CardView;

public class FOptionPane extends FDialog {
    public static final FSkinImage QUESTION_ICON = FSkinImage.QUESTION;
    public static final FSkinImage INFORMATION_ICON = FSkinImage.INFORMATION;
    public static final FSkinImage WARNING_ICON = FSkinImage.WARNING;
    public static final FSkinImage ERROR_ICON = FSkinImage.ERROR;

    public static final float PADDING = Utils.scale(10);
    public static final float GAP_BETWEEN_BUTTONS = Utils.scale(3);
    public static final float GAP_BELOW_BUTTONS = PADDING * 0.5f;
    public static final float BUTTON_HEIGHT = Utils.AVG_FINGER_HEIGHT * 0.75f;
    public static final float MIN_BUTTON_WIDTH = Utils.scale(120);

    public static float getMaxDisplayObjHeight() {
        return Forge.getCurrentScreen().getHeight() - 2 * VPrompt.HEIGHT - FDialog.TITLE_HEIGHT - 
               2 * PADDING - BUTTON_HEIGHT - GAP_BELOW_BUTTONS;
    }

    public static void showMessageDialog(String message) {
        showMessageDialog(message, "Forge", INFORMATION_ICON);
    }

    public static void showMessageDialog(String message, String title) {
        showMessageDialog(message, title, INFORMATION_ICON);
    }

    public static void showErrorDialog(String message) {
        showMessageDialog(message, "Forge", ERROR_ICON);
    }

    public static void showErrorDialog(String message, String title) {
        showMessageDialog(message, title, ERROR_ICON);
    }

    public static void showMessageDialog(String message, String title, FImage icon) {
        showOptionDialog(message, title, icon, new String[] {"OK"}, 0, null);
    }

    public static void showMessageDialog(String message, String title, FImage icon, final Callback<Integer> callback) {
        showOptionDialog(message, title, icon, new String[] {"OK"}, 0, callback);
    }

    public static void showConfirmDialog(String message, final Callback<Boolean> callback) {
        showConfirmDialog(message, "Forge", callback);
    }

    public static void showConfirmDialog(String message, String title, final Callback<Boolean> callback) {
        showConfirmDialog(message, title, "Yes", "No", true, callback);
    }

    public static void showConfirmDialog(String message, String title, boolean defaultYes, final Callback<Boolean> callback) {
        showConfirmDialog(message, title, "Yes", "No", defaultYes, callback);
    }

    public static void showConfirmDialog(String message, String title, String yesButtonText, String noButtonText, final Callback<Boolean> callback) {
        showConfirmDialog(message, title, yesButtonText, noButtonText, true, callback);
    }

    public static void showConfirmDialog(String message, String title, String yesButtonText, String noButtonText, boolean defaultYes, final Callback<Boolean> callback) {
        String[] options = {yesButtonText, noButtonText};
        showOptionDialog(message, title, QUESTION_ICON, options, defaultYes ? 0 : 1, new Callback<Integer>() {
            @Override
            public void run(Integer result) {
                callback.run(result == 0);
            }
        });
    }

    public static void showOptionDialog(String message, String title, FImage icon, String[] options, final Callback<Integer> callback) {
        showOptionDialog(message, title, icon, options, 0, callback);
    }

    public static void showOptionDialog(String message, String title, FImage icon, String[] options, int defaultOption, final Callback<Integer> callback) {
        final FOptionPane optionPane = new FOptionPane(message, title, icon, null, options, defaultOption, callback);
        optionPane.show();
    }

    public static void showCardOptionDialog(final CardView card, String message, String title, FImage icon, String[] options, int defaultOption, final Callback<Integer> callback) {
        final FDisplayObject cardDisplay;
        if (card != null) {
            cardDisplay = new FDisplayObject() {
                @Override
                public boolean tap(float x, float y, int count) {
                    CardZoom.show(card);
                    return true;
                }
                @Override
                public boolean longPress(float x, float y) {
                    CardZoom.show(card);
                    return true;
                }
                @Override
                public void draw(Graphics g) {
                    float h = getHeight();
                    float w = h / FCardPanel.ASPECT_RATIO;
                    float x = (getWidth() - w) / 2;
                    float y = 0;

                    CardRenderer.drawCard(g, card, x, y, w, h, CardStackPosition.Top);
                }
            };
            cardDisplay.setHeight(Utils.SCREEN_HEIGHT / 2);
        }
        else {
            cardDisplay = null;
        }
        final FOptionPane optionPane = new FOptionPane(message, title, icon, cardDisplay, options, defaultOption, callback);
        optionPane.show();
    }

    public static void showInputDialog(String message, String title, final Callback<String> callback) {
        showInputDialog(message, title, null, "", null, callback);
    }

    public static void showInputDialog(String message, String title, FImage icon, final Callback<String> callback) {
        showInputDialog(message, title, icon, "", null, callback);
    }

    public static void showInputDialog(String message, String title, FImage icon, String initialInput, final Callback<String> callback) {
        showInputDialog(message, title, icon, initialInput, null, callback);
    }

    public static <T> void showInputDialog(String message, String title, FImage icon, T initialInput, T[] inputOptions, final Callback<T> callback) {
        final FDisplayObject inputField;
        final FTextField txtInput;
        final FComboBox<T> cbInput;
        if (inputOptions == null) {
            txtInput = new FTextField(initialInput.toString());
            cbInput = null;
            inputField = txtInput;
        }
        else {
            txtInput = null;
            cbInput = new FComboBox<T>(inputOptions);
            cbInput.setSelectedItem(initialInput);
            inputField = cbInput;
        }

        final FOptionPane optionPane = new FOptionPane(message, title, icon, inputField, new String[] {"OK", "Cancel"}, 0, new Callback<Integer>() {
            @SuppressWarnings("unchecked")
            @Override
            public void run(Integer result) {
                if (result == 0) {
                    if (txtInput != null) {
                        callback.run((T)txtInput.getText());
                    }
                    else {
                        callback.run((T)cbInput.getSelectedItem());
                    }
                }
                else {
                    callback.run(null);
                }
            }
        });
        optionPane.show();
        if (txtInput != null) {
            txtInput.startEdit();
        }
    }

    private final FLabel lblIcon;
    private final FTextArea prompt;
    protected final FDisplayObject displayObj;
    private final FButton[] buttons;
    private final Callback<Integer> callback;
    private final int defaultOption;
    private final boolean centerIcon;

    public FOptionPane(String message, String title, FImage icon, FDisplayObject displayObj0, String[] options, int defaultOption0, final Callback<Integer> callback0) {
        super(title);

        if (icon != null) {
            centerIcon = icon.getWidth() >= 100; //for large icon, center in dialog
            lblIcon = add(new FLabel.Builder().icon(icon).iconScaleFactor(1).insets(new Vector2(0, 0)).iconInBackground(centerIcon).build());
            if (centerIcon) {
                lblIcon.setAlignment(HAlignment.CENTER);
            }
        }
        else {
            lblIcon = null;
            centerIcon = false;
        }

        if (message != null) {
            prompt = add(new FTextArea(true, message));
            prompt.setFont(FSkinFont.get(12));
            if (centerIcon) {
                prompt.setAlignment(HAlignment.CENTER);
            }
        }
        else {
            prompt = null;
        }

        displayObj = displayObj0;
        if (displayObj != null) {
            add(displayObj);
        }

        callback = callback0;

        int optionCount = options.length;
        buttons = new FButton[optionCount];
        for (int i = 0; i < optionCount; i++) {
            final int option = i;
            buttons[i] = add(new FButton(options[i], new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                    setResult(option);
                }
            }));
        }
        defaultOption = defaultOption0;
    }

    public void setResult(final int option) {
        hide();
        if (callback != null) {
            callback.run(option);
        }
    }

    public boolean isButtonEnabled(int index) {
        return buttons[index].isEnabled();
    }

    public void setButtonEnabled(int index, boolean enabled) {
        buttons[index].setEnabled(enabled);
    }

    protected boolean padDisplayObject() {
        return true;
    }

    @Override
    protected float layoutAndGetHeight(float width, float maxHeight) {
        float x = PADDING;
        float y = PADDING;

        float maxPromptHeight = maxHeight - 2 * PADDING - BUTTON_HEIGHT - GAP_BELOW_BUTTONS;
        if (displayObj != null) {
            maxPromptHeight -= displayObj.getHeight();
        }

        float promptHeight = 0;
        if (lblIcon != null) {
            float labelWidth = Utils.scale(lblIcon.getIcon().getWidth());
            promptHeight = lblIcon.getIcon().getHeight() * labelWidth / lblIcon.getIcon().getWidth();
            if (promptHeight > maxPromptHeight) {
                promptHeight = maxPromptHeight;
            }
            if (centerIcon) {
                lblIcon.setBounds(x, y, width - 2 * PADDING, promptHeight);
                y += promptHeight + PADDING;
            }
            else {
                lblIcon.setBounds(x - Utils.scale(3), y, labelWidth, promptHeight);
                x += labelWidth;
            }
        }
        if (prompt != null) {
            float promptWidth = width - x - PADDING;
            prompt.setBounds(x, y, promptWidth, prompt.getPreferredHeight(promptWidth));
            if (prompt.getHeight() < promptHeight && !centerIcon) {
                //ensure prompt centered next to icon if less tall than icon
                prompt.setTop(y + (promptHeight - prompt.getHeight()) / 2);
            }
            else if (prompt.getHeight() > maxPromptHeight) {
                prompt.setHeight(maxPromptHeight);
            }
            if (prompt.getHeight() > promptHeight || centerIcon) {
                promptHeight = prompt.getHeight();
            }
        }

        x = PADDING;
        if (promptHeight > 0) {
            y += promptHeight;
            if (displayObj == null) { //don't add additional padding between prompt and display object
                y += PADDING;
            }
        }

        if (displayObj != null) {
            if (padDisplayObject()) {
                displayObj.setBounds(x, y, width - 2 * x, displayObj.getHeight());
                y += displayObj.getHeight() + PADDING;
            }
            else {
                displayObj.setBounds(x - PADDING, y - PADDING, width, displayObj.getHeight());
                y += displayObj.getHeight();
            }
        }

        //determine size for and position buttons
        float maxButtonWidth = 0;
        for (FButton btn : buttons) {
            float buttonWidth = btn.getAutoSizeBounds().width;
            if (buttonWidth > maxButtonWidth) {
                maxButtonWidth = buttonWidth;
            }
        }
        float buttonWidth = Math.max(maxButtonWidth, MIN_BUTTON_WIDTH); //account for margins and enforce minimum width
        float dx = buttonWidth + GAP_BETWEEN_BUTTONS;
        float totalButtonWidth = dx * buttons.length - GAP_BETWEEN_BUTTONS;

        x = (width - totalButtonWidth) / 2;
        if (x < PADDING) { //reduce button width if not enough room for buttons
            buttonWidth = (width - 2 * PADDING - (buttons.length - 1) * GAP_BETWEEN_BUTTONS) / (float)buttons.length;
            x = PADDING;
            dx = buttonWidth + GAP_BETWEEN_BUTTONS;
        }
        for (FButton btn : buttons) {
            btn.setBounds(x, y, buttonWidth, BUTTON_HEIGHT);
            x += dx;
        }

        return y + BUTTON_HEIGHT + GAP_BELOW_BUTTONS;
    }

    @Override
    public boolean keyDown(int keyCode) {
        switch (keyCode) {
        case Keys.ENTER:
        case Keys.SPACE:
            if (isButtonEnabled(defaultOption)) {
                setResult(defaultOption); //set result to default option on Enter/Space
            }
            return true;
        case Keys.ESCAPE:
        case Keys.BACK:
            if (Forge.endKeyInput()) { return true; }

            if (isButtonEnabled(buttons.length - 1)) {
                setResult(buttons.length - 1); //set result to final option on Escape or Back
            }
            return true;
        }
        return super.keyDown(keyCode);
    }
}
