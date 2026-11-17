package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.FindUserUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ManageProfilePhotoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.ProfilePhotoRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.InvalidProfilePhotoException;
import com.tecnocampus.LS2.protube_back.domain.model.ProfilePhoto;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class ProfilePhotoService implements ManageProfilePhotoUseCase {
    private static final int MAX_BYTES = 5 * 1024 * 1024;
    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
    private static final Set<String> SVG_ELEMENTS = Set.of("svg", "g", "path", "rect", "circle", "ellipse",
            "line", "polyline", "polygon", "defs", "lineargradient", "radialgradient", "stop", "clippath",
            "mask", "title", "desc", "use");
    private final ProfilePhotoRepository photos;
    private final FindUserUseCase users;

    public ProfilePhotoService(ProfilePhotoRepository photos, FindUserUseCase users) {
        this.photos = photos;
        this.users = users;
    }

    @Override
    public Optional<ProfilePhoto> get(String ownerEmail) {
        return photos.findByUserId(users.getByEmail(ownerEmail).getId());
    }

    @Override
    public void update(String ownerEmail, String filename, String contentType, byte[] content) {
        User user = users.getByEmail(ownerEmail);
        if (filename == null || content == null || content.length == 0 || content.length > MAX_BYTES) {
            throw new InvalidProfilePhotoException("Profile photo must be between 1 byte and 5 MB");
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        String expectedType = switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "svg" -> "image/svg+xml";
            default -> throw new InvalidProfilePhotoException("Only PNG, JPG, JPEG, and SVG photos are supported");
        };
        if (!expectedType.equalsIgnoreCase(contentType)) {
            throw new InvalidProfilePhotoException("The file extension and content type do not match");
        }
        byte[] verifiedContent = switch (expectedType) {
            case "image/png" -> hasPngSignature(content)
                    ? content.clone()
                    : throwInvalid("The uploaded file is not a valid PNG image");
            case "image/jpeg" -> hasJpegSignature(content)
                    ? content.clone()
                    : throwInvalid("The uploaded file is not a valid JPEG image");
            default -> sanitizeSvg(content);
        };
        photos.save(user.getId(), new ProfilePhoto(expectedType, verifiedContent));
    }

    @Override
    public void delete(String ownerEmail) {
        photos.delete(users.getByEmail(ownerEmail).getId());
    }

    private static byte[] sanitizeSvg(byte[] content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
                @Override
                public void fatalError(org.xml.sax.SAXParseException exception)
                        throws org.xml.sax.SAXException {
                    throw exception;
                }
            });
            Document document = builder.parse(new ByteArrayInputStream(content));
            if (!"svg".equalsIgnoreCase(document.getDocumentElement().getLocalName())
                    || !SVG_NAMESPACE.equals(document.getDocumentElement().getNamespaceURI())) {
                throw new InvalidProfilePhotoException("The uploaded file is not a valid SVG image");
            }
            sanitizeNode(document.getDocumentElement());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            var transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(document), new StreamResult(output));
            return output.toByteArray();
        } catch (InvalidProfilePhotoException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidProfilePhotoException("The uploaded file is not a valid safe SVG image");
        }
    }

    private static void sanitizeNode(Element element) {
        String tagName = element.getLocalName().toLowerCase(Locale.ROOT);
        if (!SVG_NAMESPACE.equals(element.getNamespaceURI()) || !SVG_ELEMENTS.contains(tagName)) {
            element.getParentNode().removeChild(element);
            return;
        }
        NamedNodeMap attributes = element.getAttributes();
        for (int index = attributes.getLength() - 1; index >= 0; index--) {
            Node attribute = attributes.item(index);
            String name = attribute.getNodeName().toLowerCase(Locale.ROOT);
            String value = attribute.getNodeValue().trim().toLowerCase(Locale.ROOT);
            boolean unsafe = name.startsWith("on")
                    || name.equals("style")
                    || (name.endsWith("href") && !value.startsWith("#"))
                    || value.contains("javascript:")
                    || value.contains("data:")
                    || value.contains("@import")
                    || value.contains("expression(")
                    || (value.contains("url(") && !value.matches(".*url\\(\\s*['\"]?#[-a-z0-9_]+['\"]?\\s*\\).*"));
            if (unsafe) element.removeAttributeNode((org.w3c.dom.Attr) attribute);
        }
        NodeList children = element.getChildNodes();
        for (int index = children.getLength() - 1; index >= 0; index--) {
            Node child = children.item(index);
            if (child instanceof Element childElement) sanitizeNode(childElement);
            else if (child.getNodeType() != Node.TEXT_NODE && child.getNodeType() != Node.CDATA_SECTION_NODE) {
                element.removeChild(child);
            }
        }
    }

    private static boolean hasPngSignature(byte[] bytes) {
        byte[] signature = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        if (bytes.length < signature.length) return false;
        for (int index = 0; index < signature.length; index++) if (bytes[index] != signature[index]) return false;
        return true;
    }

    private static boolean hasJpegSignature(byte[] bytes) {
        return bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff;
    }

    private static byte[] throwInvalid(String message) {
        throw new InvalidProfilePhotoException(message);
    }
}
