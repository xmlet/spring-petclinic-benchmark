import org.openqa.selenium.By
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

def wait = new WebDriverWait(WDS.browser, Duration.ofSeconds(100), Duration.ofMillis(100))
def ownerId = WDS.vars.get('OWNER_ID')
def port = WDS.vars.get('REACT_CLIENT_PORT') ?: '4444'

// 1. Load owner details page (full page load)
WDS.browser.get("http://localhost:${port}/owners/" + ownerId)
wait.until({ d ->
    try {
        d.findElement(By.xpath("//h2[text()='Owner Information']")).isDisplayed()
    } catch (e) { false }
})

// 2. Click "Add New Pet" link to navigate to the form (React Router)
WDS.browser.findElement(By.xpath("//a[contains(normalize-space(), 'Add New Pet')]")).click()
wait.until({ d ->
    try {
        d.findElement(By.cssSelector("input[name='name']")).isDisplayed()
    } catch (e) { false }
})

// 3. Click "Cancel" link to return to owner details (React Router)
WDS.browser.findElement(By.xpath("//a[text()='Cancel']")).click()

// 4. Wait for redirect to owner details page
wait.until({ d ->
    !d.getCurrentUrl().contains('/pets/new')
})

// 5. Verify owner details page is back
wait.until({ d ->
    try {
        d.findElement(By.xpath("//h2[text()='Owner Information']")).isDisplayed()
    } catch (e) { false }
})

WDS.sampleResult.setResponseData("Pet creation canceled.")
WDS.sampleResult.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT)
WDS.sampleResult.setSuccessful(true)
