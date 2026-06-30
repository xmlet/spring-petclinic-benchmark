import org.openqa.selenium.By
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

def wait = new WebDriverWait(WDS.browser, Duration.ofSeconds(100), Duration.ofMillis(100))
def ownerId = WDS.vars.get('OWNER_ID')
def port = WDS.vars.get('HTMLFLOW_PORT') ?: '8081'

// 1. Load the owner details page (full page load)
WDS.browser.get("http://localhost:${port}/owners/" + ownerId)
wait.until({ d ->
    try {
        d.findElement(By.xpath("//h2[contains(., 'Owner Information')]")).isDisplayed()
    } catch (e) { false }
})

// 2. Click "Add New Pet" button to trigger SSE GET that patches the form into the DOM
WDS.browser.findElement(By.xpath("//button[contains(., 'Add New Pet')]")).click()
wait.until({ d ->
    try {
        d.findElement(By.cssSelector("#cancel-pet:not([disabled])")).isDisplayed()
    } catch (e) { false }
})

// Use JS click to bypass headless Chrome compositing artifacts
WDS.browser.executeScript("arguments[0].click()",
        WDS.browser.findElement(By.cssSelector("#cancel-pet")))

// 5. Wait for the form to be removed (SSE response removed #pets-add)
wait.until({ d ->
    try {
        d.findElement(By.cssSelector("#cancel-pet")).isDisplayed()
    } catch (e) { true }
})

WDS.sampleResult.setResponseData("Pet creation canceled.")
WDS.sampleResult.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT)
WDS.sampleResult.setSuccessful(true)
