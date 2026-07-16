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

    private static final String LOGO_URL   = "https://www.beta-softnet.com/logo.png";
    private static final String CAREERS_URL = "https://www.beta-softnet.com/careers";

    // ── HTML escaping ────────────────────────────────────────────────────────
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    // ── Truncate description for OG preview ──────────────────────────────────
    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> shareJob(@PathVariable Long id) {

        try {
            Optional<Job> jobOpt = jobService.getJobById(id);
            if (!jobOpt.isPresent()) {
                return ResponseEntity.status(404).contentType(MediaType.TEXT_HTML)
                        .body(notFoundPage());
            }

            Job job = jobOpt.get();

            String title       = job.getTitle()          != null ? job.getTitle()          : "Job Opening";
            String department  = job.getDepartment()     != null ? job.getDepartment()     : "";
            String location    = job.getLocation()       != null ? job.getLocation()       : "";
            String type        = job.getEmploymentType() != null ? job.getEmploymentType() : "";
            String experience  = job.getExperience()     != null ? job.getExperience()     : "";
            String salary      = job.getSalary()         != null ? job.getSalary()         : "";
            String description = job.getDescription()    != null ? job.getDescription()    : "";

            // Canonical share URL (no redirect param needed any more)
            String shareUrl   = "https://www.beta-softnet.com/share/jobs/" + id;
            String ogDesc     = truncate(description.isEmpty()
                    ? title + " at Beta Softnet" : description, 200);

            String html = buildLandingPage(
                    esc(title), esc(department), esc(location), esc(type),
                    esc(experience), esc(salary), esc(description),
                    shareUrl, esc(ogDesc)
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);

        } catch (Throwable t) {
            java.io.StringWriter sw = new java.io.StringWriter();
            t.printStackTrace(new java.io.PrintWriter(sw));
            return ResponseEntity.status(500)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<html><body><h3>Error</h3><pre>" + esc(sw.toString()) + "</pre></body></html>");
        }
    }

    // ── Landing page HTML ────────────────────────────────────────────────────
    private String buildLandingPage(
            String title, String dept, String location, String type,
            String experience, String salary, String rawDescription,
            String shareUrl, String ogDesc) {

        // Build the pills row (only non-empty values)
        StringBuilder pills = new StringBuilder();
        for (String[] pair : new String[][]{
                {"💼", dept}, {"📍", location}, {"🕒", type},
                {"⏳", experience}, {"💰", salary}}) {
            if (!pair[1].isEmpty()) {
                pills.append("<span class=\"pill\">").append(pair[0])
                     .append(" ").append(pair[1]).append("</span>");
            }
        }

        // Render description paragraphs (split on newline)
        StringBuilder descHtml = new StringBuilder();
        if (!rawDescription.isEmpty()) {
            for (String para : rawDescription.split("\\n")) {
                String p = para.trim();
                if (!p.isEmpty()) {
                    descHtml.append("<p>").append(esc(p)).append("</p>");
                }
            }
        }

        return "<!DOCTYPE html>\n"
             + "<html lang=\"en\">\n"
             + "<head>\n"
             + "  <meta charset=\"utf-8\">\n"
             + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
             + "  <title>" + title + " | Beta Softnet Careers</title>\n"

             // ── Open Graph ──────────────────────────────────────────
             + "  <meta property=\"og:type\"        content=\"website\">\n"
             + "  <meta property=\"og:url\"         content=\"" + shareUrl + "\">\n"
             + "  <meta property=\"og:title\"       content=\"" + title + " | Beta Softnet\">\n"
             + "  <meta property=\"og:description\" content=\"" + ogDesc + "\">\n"
             + "  <meta property=\"og:image\"       content=\"" + LOGO_URL + "\">\n"

             // ── Twitter Card ─────────────────────────────────────────
             + "  <meta name=\"twitter:card\"        content=\"summary_large_image\">\n"
             + "  <meta name=\"twitter:url\"         content=\"" + shareUrl + "\">\n"
             + "  <meta name=\"twitter:title\"       content=\"" + title + " | Beta Softnet\">\n"
             + "  <meta name=\"twitter:description\" content=\"" + ogDesc + "\">\n"
             + "  <meta name=\"twitter:image\"       content=\"" + LOGO_URL + "\">\n"

             // ── Inline CSS ───────────────────────────────────────────
             + "  <style>\n"
             + "    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }\n"
             + "    body {\n"
             + "      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;\n"
             + "      background: linear-gradient(135deg, #f0f4ff 0%, #fdf4ff 100%);\n"
             + "      min-height: 100vh;\n"
             + "      display: flex;\n"
             + "      align-items: center;\n"
             + "      justify-content: center;\n"
             + "      padding: 24px 16px;\n"
             + "      color: #1e293b;\n"
             + "    }\n"
             + "    .card {\n"
             + "      background: #fff;\n"
             + "      border-radius: 24px;\n"
             + "      border: 1px solid #e2e8f0;\n"
             + "      box-shadow: 0 8px 40px rgba(139,92,246,.10), 0 2px 8px rgba(0,0,0,.06);\n"
             + "      max-width: 620px;\n"
             + "      width: 100%;\n"
             + "      overflow: hidden;\n"
             + "    }\n"
             + "    .header {\n"
             + "      background: linear-gradient(135deg, #8B5CF6 0%, #EC4899 100%);\n"
             + "      padding: 32px 32px 24px;\n"
             + "      text-align: center;\n"
             + "    }\n"
             + "    .logo {\n"
             + "      height: 52px;\n"
             + "      width: auto;\n"
             + "      margin-bottom: 10px;\n"
             + "      filter: brightness(0) invert(1);\n"
             + "    }\n"
             + "    .header-label {\n"
             + "      font-size: 11px;\n"
             + "      font-weight: 800;\n"
             + "      letter-spacing: .12em;\n"
             + "      text-transform: uppercase;\n"
             + "      color: rgba(255,255,255,.80);\n"
             + "      margin-top: 4px;\n"
             + "    }\n"
             + "    .body { padding: 28px 32px 32px; }\n"
             + "    .role-label {\n"
             + "      font-size: 10px;\n"
             + "      font-weight: 800;\n"
             + "      text-transform: uppercase;\n"
             + "      letter-spacing: .12em;\n"
             + "      color: #EC4899;\n"
             + "      margin-bottom: 6px;\n"
             + "    }\n"
             + "    h1 {\n"
             + "      font-size: clamp(22px, 4vw, 30px);\n"
             + "      font-weight: 900;\n"
             + "      color: #0f172a;\n"
             + "      line-height: 1.2;\n"
             + "      margin-bottom: 16px;\n"
             + "    }\n"
             + "    .pills {\n"
             + "      display: flex;\n"
             + "      flex-wrap: wrap;\n"
             + "      gap: 8px;\n"
             + "      margin-bottom: 20px;\n"
             + "    }\n"
             + "    .pill {\n"
             + "      background: #f1f5f9;\n"
             + "      border: 1px solid #e2e8f0;\n"
             + "      color: #475569;\n"
             + "      font-size: 11px;\n"
             + "      font-weight: 700;\n"
             + "      padding: 4px 10px;\n"
             + "      border-radius: 999px;\n"
             + "    }\n"
             + "    .divider { border: none; border-top: 1px solid #f1f5f9; margin: 20px 0; }\n"
             + "    .desc-label {\n"
             + "      font-size: 10px;\n"
             + "      font-weight: 800;\n"
             + "      text-transform: uppercase;\n"
             + "      letter-spacing: .1em;\n"
             + "      color: #94a3b8;\n"
             + "      margin-bottom: 10px;\n"
             + "    }\n"
             + "    .desc {\n"
             + "      font-size: 13.5px;\n"
             + "      line-height: 1.75;\n"
             + "      color: #475569;\n"
             + "      max-height: 200px;\n"
             + "      overflow-y: auto;\n"
             + "    }\n"
             + "    .desc p { margin-bottom: 10px; }\n"
             + "    .cta-wrap { margin-top: 28px; text-align: center; }\n"
             + "    .cta {\n"
             + "      display: inline-block;\n"
             + "      background: linear-gradient(135deg, #8B5CF6 0%, #EC4899 100%);\n"
             + "      color: #fff;\n"
             + "      font-size: 14px;\n"
             + "      font-weight: 800;\n"
             + "      padding: 14px 36px;\n"
             + "      border-radius: 14px;\n"
             + "      text-decoration: none;\n"
             + "      letter-spacing: .03em;\n"
             + "      box-shadow: 0 4px 18px rgba(139,92,246,.35);\n"
             + "      transition: opacity .2s, transform .2s;\n"
             + "    }\n"
             + "    .cta:hover { opacity: .88; transform: translateY(-2px); }\n"
             + "    .footer {\n"
             + "      text-align: center;\n"
             + "      margin-top: 14px;\n"
             + "      font-size: 11px;\n"
             + "      color: #94a3b8;\n"
             + "    }\n"
             + "    @media (max-width: 480px) {\n"
             + "      .header { padding: 24px 20px 18px; }\n"
             + "      .body  { padding: 22px 20px 28px; }\n"
             + "    }\n"
             + "  </style>\n"
             + "</head>\n"
             + "<body>\n"
             + "  <div class=\"card\">\n"

             // ── Header band ──────────────────────────────────────────
             + "    <div class=\"header\">\n"
             + "      <img src=\"" + LOGO_URL + "\" alt=\"Beta Softnet\" class=\"logo\">\n"
             + "      <p class=\"header-label\">Beta Hiring</p>\n"
             + "    </div>\n"

             // ── Card body ────────────────────────────────────────────
             + "    <div class=\"body\">\n"
             + "      <p class=\"role-label\">Open Position</p>\n"
             + "      <h1>" + title + "</h1>\n"

             + (pills.length() > 0
                ? "      <div class=\"pills\">" + pills + "</div>\n"
                : "")

             + (descHtml.length() > 0
                ? "      <hr class=\"divider\">\n"
                + "      <p class=\"desc-label\">Role Summary</p>\n"
                + "      <div class=\"desc\">" + descHtml + "</div>\n"
                : "")

             + "      <div class=\"cta-wrap\">\n"
             + "        <a href=\"" + CAREERS_URL + "\" class=\"cta\">View on Careers Page &rarr;</a>\n"
             + "      </div>\n"
             + "    </div>\n"
             + "  </div>\n"
             + "  <p class=\"footer\">&copy; " + java.time.LocalDate.now().getYear()
             +                                " Beta Softnet &nbsp;&middot;&nbsp; All rights reserved.</p>\n"
             + "</body>\n"
             + "</html>\n";
    }

    // ── 404 page ─────────────────────────────────────────────────────────────
    private String notFoundPage() {
        return "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\">"
             + "<title>Job Not Found | Beta Softnet</title>"
             + "<style>body{font-family:sans-serif;display:flex;align-items:center;"
             + "justify-content:center;min-height:100vh;background:#f8fafc;color:#1e293b;}"
             + ".box{text-align:center;padding:40px;}"
             + "h2{font-size:24px;font-weight:900;margin-bottom:8px;}"
             + "p{color:#64748b;margin-bottom:24px;}"
             + "a{background:linear-gradient(135deg,#8B5CF6,#EC4899);color:#fff;"
             + "padding:12px 28px;border-radius:12px;text-decoration:none;font-weight:800;}</style>"
             + "</head><body><div class=\"box\">"
             + "<h2>Job Not Found</h2>"
             + "<p>This posting may no longer be active.</p>"
             + "<a href=\"" + CAREERS_URL + "\">Browse Open Roles &rarr;</a>"
             + "</div></body></html>";
    }
}
