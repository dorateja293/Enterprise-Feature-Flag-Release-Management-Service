import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import {
  Activity,
  AlertCircle,
  Check,
  Clock,
  ExternalLink,
  Flag,
  History,
  Loader2,
  Plus,
  RefreshCcw,
  Search,
  ShieldCheck,
  ToggleLeft,
  ToggleRight
} from 'lucide-react';
import './styles.css';

const ENVIRONMENTS = ['DEV', 'QA', 'PRODUCTION'];

const emptyFeatureForm = {
  featureName: '',
  environment: 'DEV',
  enabled: false
};

async function requestJson(path, options = {}) {
  const response = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    },
    ...options
  });

  const contentType = response.headers.get('content-type') || '';
  const payload = contentType.includes('application/json') ? await response.json() : null;

  if (!response.ok) {
    const message = payload?.message || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return payload;
}

function App() {
  const [selectedEnvironment, setSelectedEnvironment] = useState('DEV');
  const [features, setFeatures] = useState([]);
  const [releaseHistory, setReleaseHistory] = useState([]);
  const [featureForm, setFeatureForm] = useState(emptyFeatureForm);
  const [searchTerm, setSearchTerm] = useState('');
  const [loadingFeatures, setLoadingFeatures] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [savingFeature, setSavingFeature] = useState(false);
  const [togglingKey, setTogglingKey] = useState('');
  const [notice, setNotice] = useState(null);

  const showNotice = useCallback((type, message) => {
    setNotice({ type, message });
  }, []);

  const loadFeatures = useCallback(async (environment = selectedEnvironment) => {
    setLoadingFeatures(true);
    try {
      const data = await requestJson(`/api/features/${environment}`);
      setFeatures(data);
    } catch (error) {
      showNotice('error', error.message);
    } finally {
      setLoadingFeatures(false);
    }
  }, [selectedEnvironment, showNotice]);

  const loadReleaseHistory = useCallback(async () => {
    setLoadingHistory(true);
    try {
      const data = await requestJson('/api/release-history');
      setReleaseHistory(data);
    } catch (error) {
      showNotice('error', error.message);
    } finally {
      setLoadingHistory(false);
    }
  }, [showNotice]);

  useEffect(() => {
    loadFeatures(selectedEnvironment);
  }, [loadFeatures, selectedEnvironment]);

  useEffect(() => {
    loadReleaseHistory();
  }, [loadReleaseHistory]);

  const filteredFeatures = useMemo(() => {
    const query = searchTerm.trim().toLowerCase();

    if (!query) {
      return features;
    }

    return features.filter((feature) => feature.featureName.toLowerCase().includes(query));
  }, [features, searchTerm]);

  const enabledCount = useMemo(
    () => features.filter((feature) => feature.enabled).length,
    [features]
  );

  const disabledCount = features.length - enabledCount;

  const latestRelease = releaseHistory[0];

  async function handleCreateFeature(event) {
    event.preventDefault();
    setSavingFeature(true);

    try {
      const createdFeature = await requestJson('/api/feature', {
        method: 'POST',
        body: JSON.stringify(featureForm)
      });

      showNotice('success', `${createdFeature.featureName} created in ${createdFeature.environment}`);
      setFeatureForm({ ...emptyFeatureForm, environment: selectedEnvironment });
      await loadFeatures(selectedEnvironment);
    } catch (error) {
      showNotice('error', error.message);
    } finally {
      setSavingFeature(false);
    }
  }

  async function handleToggleFeature(feature) {
    const key = `${feature.featureName}:${feature.environment}`;
    setTogglingKey(key);

    try {
      await requestJson('/api/toggle-feature', {
        method: 'PUT',
        body: JSON.stringify({
          featureName: feature.featureName,
          environment: feature.environment,
          enabled: !feature.enabled
        })
      });

      showNotice('success', `${feature.featureName} toggled to ${!feature.enabled ? 'enabled' : 'disabled'}`);
      await Promise.all([loadFeatures(selectedEnvironment), loadReleaseHistory()]);
    } catch (error) {
      showNotice('error', error.message);
    } finally {
      setTogglingKey('');
    }
  }

  function handleEnvironmentChange(environment) {
    setSelectedEnvironment(environment);
    setFeatureForm((current) => ({ ...current, environment }));
  }

  return (
    <main className="app-shell">
      <header className="top-bar">
        <div>
          <p className="eyebrow">Release Management</p>
          <h1>Feature Flags</h1>
        </div>
        <div className="top-actions">
          <a className="link-button" href="/swagger-ui.html" target="_blank" rel="noreferrer">
            <ExternalLink size={16} />
            API Docs
          </a>
          <button className="icon-button" type="button" onClick={() => Promise.all([loadFeatures(), loadReleaseHistory()])} title="Refresh data">
            <RefreshCcw size={18} />
          </button>
        </div>
      </header>

      {notice && (
        <section className={`notice ${notice.type}`}>
          {notice.type === 'success' ? <Check size={18} /> : <AlertCircle size={18} />}
          <span>{notice.message}</span>
          <button type="button" onClick={() => setNotice(null)}>Dismiss</button>
        </section>
      )}

      <section className="metrics-grid">
        <Metric icon={Flag} label="Total Flags" value={features.length} />
        <Metric icon={ToggleRight} label="Enabled" value={enabledCount} tone="green" />
        <Metric icon={ToggleLeft} label="Disabled" value={disabledCount} tone="amber" />
        <Metric icon={History} label="Audit Events" value={releaseHistory.length} />
      </section>

      <section className="workspace">
        <aside className="side-panel">
          <div className="panel-heading">
            <ShieldCheck size={18} />
            <h2>Create Flag</h2>
          </div>

          <form className="feature-form" onSubmit={handleCreateFeature}>
            <label>
              Feature Name
              <input
                value={featureForm.featureName}
                onChange={(event) => setFeatureForm((current) => ({ ...current, featureName: event.target.value }))}
                placeholder="dark-mode"
                maxLength={120}
                required
              />
            </label>

            <label>
              Environment
              <select
                value={featureForm.environment}
                onChange={(event) => setFeatureForm((current) => ({ ...current, environment: event.target.value }))}
              >
                {ENVIRONMENTS.map((environment) => (
                  <option key={environment} value={environment}>{environment}</option>
                ))}
              </select>
            </label>

            <label className="check-row">
              <input
                type="checkbox"
                checked={featureForm.enabled}
                onChange={(event) => setFeatureForm((current) => ({ ...current, enabled: event.target.checked }))}
              />
              Start enabled
            </label>

            <button className="primary-button" type="submit" disabled={savingFeature}>
              {savingFeature ? <Loader2 className="spin" size={16} /> : <Plus size={16} />}
              Create Feature
            </button>
          </form>

          <div className="latest-release">
            <div className="panel-heading compact">
              <Clock size={18} />
              <h2>Latest Toggle</h2>
            </div>
            {latestRelease ? (
              <div className="release-summary">
                <strong>{latestRelease.featureName}</strong>
                <span>{latestRelease.environment}</span>
                <p>{String(latestRelease.oldStatus)} to {String(latestRelease.newStatus)}</p>
              </div>
            ) : (
              <p className="muted">No release events yet.</p>
            )}
          </div>
        </aside>

        <section className="main-panel">
          <div className="toolbar">
            <div className="segment-control" aria-label="Environment filter">
              {ENVIRONMENTS.map((environment) => (
                <button
                  key={environment}
                  type="button"
                  className={environment === selectedEnvironment ? 'active' : ''}
                  onClick={() => handleEnvironmentChange(environment)}
                >
                  {environment}
                </button>
              ))}
            </div>

            <label className="search-box">
              <Search size={16} />
              <input
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
                placeholder="Search flags"
              />
            </label>
          </div>

          <div className="feature-list">
            {loadingFeatures ? (
              <EmptyState icon={Loader2} text="Loading feature flags" spin />
            ) : filteredFeatures.length === 0 ? (
              <EmptyState icon={Flag} text="No feature flags found" />
            ) : (
              filteredFeatures.map((feature) => {
                const toggleKey = `${feature.featureName}:${feature.environment}`;
                const isToggling = togglingKey === toggleKey;

                return (
                  <article className="feature-row" key={toggleKey}>
                    <div>
                      <h3>{feature.featureName}</h3>
                      <p>Created {formatDate(feature.createdAt)}</p>
                    </div>
                    <span className={`status-pill ${feature.enabled ? 'enabled' : 'disabled'}`}>
                      {feature.enabled ? 'Enabled' : 'Disabled'}
                    </span>
                    <button
                      className={`toggle-button ${feature.enabled ? 'on' : ''}`}
                      type="button"
                      onClick={() => handleToggleFeature(feature)}
                      disabled={isToggling}
                      title={`Toggle ${feature.featureName}`}
                    >
                      {isToggling ? <Loader2 className="spin" size={18} /> : feature.enabled ? <ToggleRight size={22} /> : <ToggleLeft size={22} />}
                    </button>
                  </article>
                );
              })
            )}
          </div>
        </section>
      </section>

      <section className="history-panel">
        <div className="panel-title-row">
          <div className="panel-heading">
            <Activity size={18} />
            <h2>Release History</h2>
          </div>
          {loadingHistory && <Loader2 className="spin" size={18} />}
        </div>

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Feature</th>
                <th>Environment</th>
                <th>Old</th>
                <th>New</th>
                <th>Timestamp</th>
              </tr>
            </thead>
            <tbody>
              {releaseHistory.length === 0 ? (
                <tr>
                  <td colSpan="5" className="empty-cell">No release history available.</td>
                </tr>
              ) : (
                releaseHistory.map((event) => (
                  <tr key={event.id}>
                    <td>{event.featureName}</td>
                    <td>{event.environment}</td>
                    <td>{String(event.oldStatus)}</td>
                    <td>{String(event.newStatus)}</td>
                    <td>{formatDate(event.timestamp)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  );
}

function Metric({ icon: Icon, label, value, tone = 'blue' }) {
  return (
    <div className={`metric ${tone}`}>
      <Icon size={20} />
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function EmptyState({ icon: Icon, text, spin = false }) {
  return (
    <div className="empty-state">
      <Icon className={spin ? 'spin' : ''} size={24} />
      <span>{text}</span>
    </div>
  );
}

function formatDate(value) {
  if (!value) {
    return '-';
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value));
}

createRoot(document.getElementById('root')).render(<App />);
