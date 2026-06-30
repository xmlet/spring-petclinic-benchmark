import org.openqa.selenium.By
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

def wait = new WebDriverWait(WDS.browser, Duration.ofSeconds(100), Duration.ofMillis(100))
def port = WDS.vars.get('THYMELEAF_PORT') ?: '8080'

// 1. Load the find owners page (full page load)
WDS.browser.get("http://localhost:${port}/owners/find")
wait.until({ d ->
    try {
        d.findElement(By.xpath("//h2[text()='Find Owners']")).isDisplayed()
    } catch (e) { false }
})

// 2. fill input with "es"
def lastNameInput = WDS.browser.findElement(By.cssSelector("input[name='lastName']"))
lastNameInput.clear()
lastNameInput.sendKeys("es")

// 3. click button with text "Find Owner"
WDS.browser.findElement(By.xpath("//button[normalize-space()='Find Owner']")).click()

// 4. verify table size is 2 (wait for full page navigation)
wait.until({ d ->
    try {
        def rows = d.findElements(By.cssSelector("#owners tbody tr"))
        rows.size() == 2
    } catch (e) { false }
})

// 5. verify names are: Maria Escobito and Carlos Estaban
def rows = WDS.browser.findElements(By.cssSelector("#owners tbody tr"))
def names = rows.collect { row ->
    row.findElement(By.xpath(".//td[1]/a")).getText()
}
def found1 = names.contains("Maria Escobito")
def found2 = names.contains("Carlos Estaban")

WDS.sampleResult.setResponseData("Owners with 'es' listed: $names")
WDS.sampleResult.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT)
WDS.sampleResult.setSuccessful(found1 && found2)
