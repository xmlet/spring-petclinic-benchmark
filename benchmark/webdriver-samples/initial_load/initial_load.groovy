import org.openqa.selenium.By
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

def wait = new WebDriverWait(WDS.browser, Duration.ofSeconds(100), Duration.ofMillis(100))
def ownerId = WDS.vars.get('OWNER_ID')

WDS.browser.get("http://localhost:${port}/owners/" + ownerId)

wait.until({ d ->
    try {
        def heading = d.findElement(By.xpath("//h2[contains(., 'Owner Information')]"))
        return heading.isDisplayed()
    } catch (e) { false }
})

WDS.sampleResult.setResponseData("Initial page load completed")
WDS.sampleResult.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT)
WDS.sampleResult.setSuccessful(true)
