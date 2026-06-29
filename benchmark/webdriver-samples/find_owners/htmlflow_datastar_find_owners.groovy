import org.openqa.selenium.By
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

def wait = new WebDriverWait(WDS.browser, Duration.ofSeconds(100))
def port = WDS.vars.get('HTMLFLOW_PORT') ?: '8081'

// 1. Load the owners page (full page load)
WDS.browser.get("http://localhost:${port}/owners")
wait.until({ d ->
    try {
        def heading = d.findElement(By.xpath("//h2[contains(., 'Owners')]"))
        return heading.isDisplayed()
    } catch (e) { false }
})

// 2. verify initial table is empty
wait.until({ d ->
    try {
        def rows = d.findElements(By.cssSelector("#owners tbody tr"))
        rows.size() == 0
    } catch (e) { false }
})

// 3. Fill search bar input with "es" in a single shot
def searchInput = WDS.browser.findElement(By.cssSelector("input[name='lastName']"))
WDS.browser.executeScript("""
    arguments[0].value = 'es';
    arguments[0].dispatchEvent(new Event('input', {bubbles: true}));
""", searchInput)

// 4. verify table size is 2 (wait for DataStar SSE + patch)
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
