package game.general_luna_game;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import java.io.InputStream;

public class CharacterSelectionController {
    @FXML
    private Button pistolCharacterButton;
    @FXML
    private Button rifleCharacterButton;
    @FXML
    private Button back;

    private GeneralLunaGame app;

    public void setGameApp(GeneralLunaGame app) {
        this.app = app;
    }

    @FXML
    public void initialize() {
        pistolCharacterButton.setOnAction(event -> startGameWithCharacter("Pistol"));
        rifleCharacterButton.setOnAction(event -> startGameWithCharacter("Rifle"));
        back.setOnAction(event -> {
            try {
                app.showHomeScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startGameWithCharacter(String characterType) {
        String imagePath = "";

        switch (characterType) {
            case "Pistol":
                imagePath = "/images/pistol_character.png";
                break;
            case "Rifle":
                imagePath = "/images/rifle_character.png";
                break;
            default:
                imagePath = "images/default_character.png";
                break;
        }

        InputStream is = getClass().getResourceAsStream(imagePath);
        if (is == null) {
            System.out.println("Image not found at path: " + imagePath);
            is = getClass().getResourceAsStream("/images/pistol.png");
        }

        Image characterImage = new Image(is);

        Hero hero = new Hero(characterType, 100, 100, characterImage);

        try {
            app.showGameScreen(hero);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}