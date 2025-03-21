package me.gnevilkoko.project_manager.models.services;

import jakarta.annotation.Nullable;
import me.gnevilkoko.project_manager.models.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

@Service
public class AvatarStorageService {
    private String storagePath;

    @Autowired
    public AvatarStorageService(@Value("${users.storage.path}") String storagePath) {
        this.storagePath = System.getProperty("user.dir")+storagePath;
    }

    @Nullable
    public String getAvatarFilePath(User user){
        File userFolder = new File(storagePath + "/" + user.getUsername().toLowerCase());
        String avatarFileName = null;

        for(File file : userFolder.listFiles()) {
            if(file.isFile() && file.getName().startsWith("avatar")){
                avatarFileName = file.getName();
                break;
            }
        }

        return storagePath + "/" + user.getUsername().toLowerCase()+"/"+avatarFileName;
    }

    public byte[] getAvatar(User user) throws IOException {
        File userFolder = new File(storagePath + "/" + user.getUsername().toLowerCase());
        if(!userFolder.exists()){
            userFolder.mkdirs();
            generateAvatar(user);
        }

        String avatarFileName = getAvatarFilePath(user);

        File avatarFile = new File(getAvatarFilePath(user));
        if(!avatarFile.exists()){
            generateAvatar(user);
        }
        return Files.readAllBytes(avatarFile.toPath());
    }

    public boolean uploadCustomAvatar(User user, MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        String fileExtension;
        if (contentType.equals("image/png")) {
            fileExtension = ".png";
        } else if (contentType.equals("image/jpeg")) {
            fileExtension = ".jpg";
        } else {
            return false;
        }

        String resultFilePath = storagePath + "/" + user.getUsername().toLowerCase() + "/avatar" + fileExtension;
        File resultFile = new File(resultFilePath);
        File parentFile = new File(resultFile.getParent());
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        } else {
            String path = getAvatarFilePath(user);
            if(path != null){
                File alreadyExistFile = new File(path);
                boolean deleteRes = alreadyExistFile.delete();
                if(!deleteRes){
                    return false;
                }
            }

        }

        Files.copy(file.getInputStream(), resultFile.toPath());
        return true;
    }


    public boolean generateAvatar(User user) throws IOException {
        int width = 200;
        int height = 200;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color background = getRandomColor();
        g2d.setColor(background);
        g2d.fillRect(0, 0, width, height);

        Color textColor = getRandomContrastingColor(background, 4.5);

        String initials = "";
        if (user.getName() != null && !user.getName().isEmpty()) {
            initials += user.getName().substring(0, 1).toUpperCase();
        }
        if (user.getSurname() != null && !user.getSurname().isEmpty()) {
            initials += user.getSurname().substring(0, 1).toUpperCase();
        }

        g2d.setColor(textColor);

        int fontSize = width / 2;
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);

        FontRenderContext frc = g2d.getFontRenderContext();
        Rectangle2D textBounds = font.getStringBounds(initials, frc);
        int textWidth = (int) Math.round(textBounds.getWidth());
        int textHeight = (int) Math.round(textBounds.getHeight());
        int x = (width - textWidth) / 2;
        int y = (height - textHeight) / 2 + (int) -textBounds.getY();

        g2d.drawString(initials, x, y);
        g2d.dispose();

        String resultFilePath = storagePath + "/" + user.getUsername().toLowerCase() + "/avatar.png";
        File resultFile = new File(resultFilePath);
        File parentDir = resultFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        ImageIO.write(image, "png", resultFile);
        return true;
    }

    private Color getRandomColor() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return new Color(r, g, b);
    }

    private Color getRandomContrastingColor(Color background, double minContrastRatio) {
        Random random = new Random();
        Color textColor;
        int attempt = 0;
        do {
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);
            textColor = new Color(r, g, b);
            attempt++;
            if (attempt > 1000) {
                textColor = getContrastRatio(background, Color.WHITE) >= minContrastRatio ? Color.WHITE : Color.BLACK;
                break;
            }
        } while (getContrastRatio(background, textColor) < minContrastRatio);
        return textColor;
    }

    private double getLuminance(Color color) {
        double r = color.getRed() / 255.0;
        double g = color.getGreen() / 255.0;
        double b = color.getBlue() / 255.0;
        r = (r <= 0.03928) ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.03928) ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.03928) ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private double getContrastRatio(Color color1, Color color2) {
        double lum1 = getLuminance(color1);
        double lum2 = getLuminance(color2);
        double brighter = Math.max(lum1, lum2);
        double darker = Math.min(lum1, lum2);
        return (brighter + 0.05) / (darker + 0.05);
    }
}
