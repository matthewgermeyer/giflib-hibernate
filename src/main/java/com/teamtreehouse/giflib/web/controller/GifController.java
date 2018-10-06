package com.teamtreehouse.giflib.web.controller;

import com.teamtreehouse.giflib.model.Gif;
import com.teamtreehouse.giflib.service.CategoryService;
import com.teamtreehouse.giflib.service.GifService;
import com.teamtreehouse.giflib.web.FlashMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
public class GifController {
    @Autowired
    private GifService gifService;

    @Autowired
    private CategoryService categoryService;

    // Home page - index of all GIFs
    @RequestMapping("/")
    public String listGifs(Model model) {

        List<Gif> gifs = gifService.findAll();

        model.addAttribute("gifs", gifs);
        return "gif/index";
    }

    // Single GIF page
    @RequestMapping("/gifs/{gifId}")
    public String gifDetails(@PathVariable Long gifId, Model model) {
        //Get gif whose id is gifId
        Gif gif = gifService.findById(gifId);

        model.addAttribute("gif", gif);
        return "gif/details";
    }

    // GIF image data
    @RequestMapping("/gifs/{gifId}.gif")
    @ResponseBody
    public byte[] gifImage(@PathVariable Long gifId) {
        //Return image data as byte array of the GIF whose id is gifId
        return gifService.findById(gifId).getBytes();
    }

    // Favorites - index of all GIFs marked favorite
    @RequestMapping("/favorites")
    public String favorites(Model model) {

        List<Gif> faves = gifService.getFavorites();

        model.addAttribute("gifs",faves);
        model.addAttribute("username","MattyTrane");
        return "gif/favorites";
    }

    // Upload a new GIF
    @RequestMapping(value = "/gifs", method = RequestMethod.POST)
    public String addGif(@Valid Gif gif, @RequestParam MultipartFile file, RedirectAttributes redirectAttributes, BindingResult result) {

        if (result.hasErrors()) {
            // Include validation errors upon redirect
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.gif",result);

            // Add  category if invalid was received
            redirectAttributes.addFlashAttribute("gif",gif);

            // Redirect back to the form
            return String.format("redirect:/gifs/%s/edit",gif.getId());

        }
        //Upload new GIF if data is valid
        gifService.save(gif,file);

        // Add flash message for success
        redirectAttributes.addFlashAttribute("flash",new FlashMessage("GIF successfully uploaded!", FlashMessage.Status.SUCCESS));

        //Redirect browser to new GIF's detail view
        return String.format("redirect:/gifs/%s",gif.getId());
    }

    // Form for uploading a new GIF
    @RequestMapping("/upload")
    public String formNewGif(Model model) {
        // check if model has a "gif" in it (from validation check failed -> redirect)
        if(!model.containsAttribute("gif")) {
            model.addAttribute("gif",new Gif());
        }
        model.addAttribute("categories",categoryService.findAll());
        model.addAttribute("action","/gifs");
        model.addAttribute("heading","Upload");
        model.addAttribute("submit","Add");

        return "gif/form";
    }

    // Form for editing an existing GIF
    @RequestMapping(value = "/gifs/{gifId}/edit")
    public String formEditGif(@PathVariable Long gifId, Model model) {
        if(!model.containsAttribute("gif")) {
            model.addAttribute("gif",gifService.findById(gifId));
        }
        model.addAttribute("categories",categoryService.findAll());
        model.addAttribute("action",String.format("/gifs/%s",gifId));
        model.addAttribute("heading","Edit GIF");
        model.addAttribute("submit","Update");

        return "gif/form";
    }

    // Update an existing GIF
    @RequestMapping(value = "/gifs/{gifId}", method = RequestMethod.POST)
    public String updateGif(@Valid Gif gif, @RequestParam MultipartFile file, RedirectAttributes redirectAttributes, BindingResult result) {

        if (result.hasErrors()) {
            // Include validation errors upon redirect
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.category",result);

            // Add  category if invalid was received
            redirectAttributes.addFlashAttribute("gif",gif);

            // Redirect back to the form
            return String.format("redirect:/gifs/%s/edit",gif.getId());

        }
        gifService.save(gif,file);
        // Flash message
        redirectAttributes.addFlashAttribute("flash",new FlashMessage("GIF successfully updated!", FlashMessage.Status.SUCCESS));

        // Redirect browser to updated GIF's detail view
        return String.format("redirect:/gifs/%s",gif.getId());
    }

    // Delete an existing GIF
    @RequestMapping(value = "/gifs/{gifId}/delete", method = RequestMethod.POST)
    public String deleteGif(@PathVariable Long gifId, RedirectAttributes redirectAttributes) {
        Gif gif = gifService.findById(gifId);
        gifService.delete(gif);
        redirectAttributes.addFlashAttribute("flash",new FlashMessage("GIF deleted!", FlashMessage.Status.SUCCESS));

        return "redirect:/";
    }

    // Mark/unmark an existing GIF as a favorite
    @RequestMapping(value = "/gifs/{gifId}/favorite", method = RequestMethod.POST)
    public String toggleFavorite(@PathVariable Long gifId, HttpServletRequest request) {

        Gif gif = gifService.findById(gifId);
        gifService.toggleFavorite(gif);
        //TODO: Read Docs - 'Referer'
        return String.format("redirect:%s",request.getHeader("referer"));
    }

    // Search results
    @RequestMapping("/search")
    public String searchResults(@RequestParam String q, Model model) {
        // TODO: Get list of GIFs whose description contains value specified by q.  Read Spring Docs.
        List<Gif> gifs = new ArrayList<>();

        model.addAttribute("gifs",gifs);
        return "gif/index";
    }
}