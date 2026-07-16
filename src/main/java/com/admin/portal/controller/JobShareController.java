package com.admin.portal.controller;

import com.admin.portal.entity.Job;
import com.admin.portal.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/share/jobs")
public class JobShareController {

    @Autowired
    private JobService jobService;

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> shareJob(
            @PathVariable Long id,
            @RequestParam(value = "redirect", required = false) String redirect) {

        try {
            Optional<Job> jobOpt = jobService.getJobById(id);
            if (!jobOpt.isPresent()) {
                return ResponseEntity.status(404).body("<html><body><h3>Job Posting Not Found</h3></body></html>");
            }

            Job job = jobOpt.get();
            String title = job.getTitle() != null ? job.getTitle() : "Job Opening";

            // Escape double quotes to prevent breaking meta tag attributes
            title = title.replace("\"", "&quot;");

            // URL decode the redirect parameter if present to resolve any double encoding
            String decodedRedirect = redirect;
            if (decodedRedirect != null) {
                try {
                    decodedRedirect = java.net.URLDecoder.decode(decodedRedirect, "UTF-8");
                } catch (Exception e) {
                    // Fallback to original
                }
            }

            // Determine redirect target URL (fallbacks to default dev frontend)
            String targetUrl = decodedRedirect != null ? decodedRedirect : "http://localhost:5173/careers";
            if (targetUrl.contains("?")) {
                targetUrl += "&job=" + id;
            } else {
                targetUrl += "?job=" + id;
            }

            // Parse frontend origin to construct absolute logo URL
            String logoUrl = "http://localhost:5173/logo.png";
            if (decodedRedirect != null) {
                try {
                    java.net.URI uri = new java.net.URI(decodedRedirect);
                    String scheme = uri.getScheme();
                    String authority = uri.getAuthority();
                    if (scheme != null && authority != null) {
                        logoUrl = scheme + "://" + authority + "/logo.png";
                    }
                } catch (Exception e) {
                    // Fallback to default logo
                }
            }

            // As requested, the description/preview text should display the job link itself
            String description = targetUrl;

            String html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset=\"utf-8\">\n" +
                    "    <title>" + title + " | Beta Softnet</title>\n" +
                    "    <!-- Open Graph / Facebook -->\n" +
                    "    <meta property=\"og:type\" content=\"website\">\n" +
                    "    <meta property=\"og:url\" content=\"" + targetUrl + "\">\n" +
                    "    <meta property=\"og:title\" content=\"" + title + " | Beta Softnet\">\n" +
                    "    <meta property=\"og:description\" content=\"" + description + "\">\n" +
                    "    <meta property=\"og:image\" content=\"" + logoUrl + "\">\n" +
                    "    <!-- Twitter -->\n" +
                    "    <meta property=\"twitter:card\" content=\"summary_large_image\">\n" +
                    "    <meta property=\"twitter:url\" content=\"" + targetUrl + "\">\n" +
                    "    <meta property=\"twitter:title\" content=\"" + title + " | Beta Softnet\">\n" +
                    "    <meta property=\"twitter:description\" content=\"" + description + "\">\n" +
                    "    <meta property=\"twitter:image\" content=\"" + logoUrl + "\">\n" +
                    "    <script>\n" +
                    "        window.location.href = \"" + targetUrl + "\";\n" +
                    "    </script>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <p>Redirecting to job page...</p>\n" +
                    "</body>\n" +
                    "</html>";

            return ResponseEntity.ok(html);
        } catch (Throwable t) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            t.printStackTrace(pw);
            return ResponseEntity.status(500)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<html><body><h3>Exception Occurred</h3><pre>" + sw.toString() + "</pre></body></html>");
        }
    }
}
